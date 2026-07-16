package dev.intentflow.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal sealed interface LoginState {
    data object Idle : LoginState
    data class Validating(val email: String) : LoginState
    data object RequestingToken : LoginState
    data object WaitingForTwoFactor : LoginState
    data class Failed(val message: String) : LoginState
    data class Authenticated(val userId: String) : LoginState
}

internal sealed interface LoginIntent {
    data class Submit(val email: String, val password: String) : LoginIntent
    data class SubmitTwoFactor(val code: String) : LoginIntent
    data object Cancel : LoginIntent
}

internal sealed interface LoginEvent {
    data object CredentialsValid : LoginEvent
    data object TokenRequiresTwoFactor : LoginEvent
    data class TokenReceived(val userId: String) : LoginEvent
    data class TokenFailed(val message: String) : LoginEvent
}

internal sealed interface LoginEffect {
    data class ValidateCredentials(val email: String, val password: String) : LoginEffect
    data class RequestToken(val email: String) : LoginEffect
}

internal sealed interface LoginOutput {
    data class Completed(val userId: String) : LoginOutput
    data object Cancelled : LoginOutput
}

internal sealed interface LoginRoute {
    data object TwoFactor : LoginRoute
}

internal class LoginFlow :
    FlowReducer<LoginState, LoginIntent, LoginEvent, LoginEffect, LoginOutput, LoginRoute> {
    override fun reduce(
        state: LoginState,
        signal: FlowSignal<LoginIntent, LoginEvent>
    ): Next<LoginState, LoginEffect, LoginOutput, LoginRoute> =
        when (signal) {
            is FlowSignal.IntentSignal -> reduceIntent(state, signal.intent)
            is FlowSignal.EventSignal -> reduceEvent(state, signal.event)
        }

    private fun reduceIntent(
        state: LoginState,
        intent: LoginIntent
    ): Next<LoginState, LoginEffect, LoginOutput, LoginRoute> =
        when {
            state is LoginState.Idle && intent is LoginIntent.Submit ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(
                    LoginState.Validating(intent.email)
                ).effect(
                    LoginEffect.ValidateCredentials(intent.email, intent.password),
                    id = EffectId("login.validate"),
                    policy = EffectPolicy.CancelInFlight
                )

            intent is LoginIntent.Cancel ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(LoginState.Idle)
                    .cancel(EffectId("login.validate"))
                    .cancel(EffectId("login.token"))
                    .output(LoginOutput.Cancelled)

            else -> Next.state(state)
        }

    private fun reduceEvent(
        state: LoginState,
        event: LoginEvent
    ): Next<LoginState, LoginEffect, LoginOutput, LoginRoute> =
        when {
            state is LoginState.Validating && event is LoginEvent.CredentialsValid ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(
                    LoginState.RequestingToken
                ).effect(
                    LoginEffect.RequestToken(state.email),
                    id = EffectId("login.token"),
                    policy = EffectPolicy.CancelInFlight
                )

            state is LoginState.RequestingToken && event is LoginEvent.TokenRequiresTwoFactor ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(
                    LoginState.WaitingForTwoFactor
                ).route(LoginRoute.TwoFactor)

            state is LoginState.RequestingToken && event is LoginEvent.TokenReceived ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(
                    LoginState.Authenticated(event.userId)
                ).output(LoginOutput.Completed(event.userId))

            event is LoginEvent.TokenFailed ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(
                    LoginState.Failed(event.message)
                )

            else -> Next.state(state)
        }
}

internal class LoginEffects : FlowEffectHandler<LoginEffect, LoginEvent> {
    override fun handle(effect: LoginEffect): Flow<LoginEvent> =
        flow {
            when (effect) {
                is LoginEffect.ValidateCredentials -> emit(LoginEvent.CredentialsValid)
                is LoginEffect.RequestToken -> emit(LoginEvent.TokenReceived("user-1"))
            }
        }
}
