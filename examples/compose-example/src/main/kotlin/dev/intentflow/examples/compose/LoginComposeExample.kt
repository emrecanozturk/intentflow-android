package dev.intentflow.examples.compose

import dev.intentflow.core.EffectId
import dev.intentflow.core.EffectPolicy
import dev.intentflow.core.FlowEffectHandler
import dev.intentflow.core.FlowProjection
import dev.intentflow.core.FlowReducer
import dev.intentflow.core.FlowSignal
import dev.intentflow.core.FlowStore
import dev.intentflow.core.Next
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow

public sealed interface LoginState {
    public data object Idle : LoginState
    public data class Validating(public val email: String) : LoginState
    public data object WaitingForToken : LoginState
    public data object WaitingForTwoFactor : LoginState
    public data class Failed(public val message: String) : LoginState
    public data class Authenticated(public val userId: String) : LoginState
}

public sealed interface LoginIntent {
    public data class Submit(public val email: String, public val password: String) : LoginIntent
    public data class SubmitTwoFactor(public val code: String) : LoginIntent
    public data object Cancel : LoginIntent
}

public sealed interface LoginEvent {
    public data object CredentialsValid : LoginEvent
    public data object TokenRequiresTwoFactor : LoginEvent
    public data class TokenReceived(public val userId: String) : LoginEvent
    public data class Failed(public val message: String) : LoginEvent
}

public sealed interface LoginEffect {
    public data class Validate(public val email: String, public val password: String) : LoginEffect
    public data class RequestToken(public val email: String) : LoginEffect
}

public sealed interface LoginOutput {
    public data class Completed(public val userId: String) : LoginOutput
}

public sealed interface LoginRoute {
    public data object TwoFactor : LoginRoute
}

public class LoginFlow :
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
                    LoginEffect.Validate(intent.email, intent.password),
                    id = EffectId("login.validate"),
                    policy = EffectPolicy.CancelInFlight
                )

            intent is LoginIntent.Cancel ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(LoginState.Idle)
                    .cancel(EffectId("login.validate"))
                    .cancel(EffectId("login.token"))

            else -> Next.state(state)
        }

    private fun reduceEvent(
        state: LoginState,
        event: LoginEvent
    ): Next<LoginState, LoginEffect, LoginOutput, LoginRoute> =
        when {
            state is LoginState.Validating && event is LoginEvent.CredentialsValid ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(
                    LoginState.WaitingForToken
                ).effect(
                    LoginEffect.RequestToken(state.email),
                    id = EffectId("login.token"),
                    policy = EffectPolicy.CancelInFlight
                )

            state is LoginState.WaitingForToken && event is LoginEvent.TokenRequiresTwoFactor ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(
                    LoginState.WaitingForTwoFactor
                ).route(LoginRoute.TwoFactor)

            state is LoginState.WaitingForToken && event is LoginEvent.TokenReceived ->
                Next.state<LoginState, LoginEffect, LoginOutput, LoginRoute>(
                    LoginState.Authenticated(event.userId)
                ).output(LoginOutput.Completed(event.userId))

            event is LoginEvent.Failed -> Next.state(LoginState.Failed(event.message))

            else -> Next.state(state)
        }
}

public class LoginEffects : FlowEffectHandler<LoginEffect, LoginEvent> {
    override fun handle(effect: LoginEffect): Flow<LoginEvent> =
        flow {
            when (effect) {
                is LoginEffect.Validate -> emit(LoginEvent.CredentialsValid)
                is LoginEffect.RequestToken -> emit(LoginEvent.TokenReceived("user-1"))
            }
        }
}

public data class LoginViewState(
    public val title: String,
    public val isLoading: Boolean,
    public val errorMessage: String?
)

public class LoginProjection : FlowProjection<LoginState, LoginViewState> {
    override fun project(state: LoginState): LoginViewState =
        when (state) {
            LoginState.Idle -> LoginViewState("Sign in", isLoading = false, errorMessage = null)
            is LoginState.Validating,
            LoginState.WaitingForToken -> LoginViewState("Signing in", isLoading = true, errorMessage = null)
            LoginState.WaitingForTwoFactor -> LoginViewState("Two-factor code", isLoading = false, errorMessage = null)
            is LoginState.Failed -> LoginViewState("Try again", isLoading = false, errorMessage = state.message)
            is LoginState.Authenticated -> LoginViewState("Welcome", isLoading = false, errorMessage = null)
        }
}

public class LoginComposeAdapter(
    private val store: FlowStore<LoginState, LoginIntent, LoginEvent, LoginEffect, LoginOutput, LoginRoute>,
    private val projection: LoginProjection = LoginProjection()
) {
    public val viewState = store.snapshots.map { projection.project(it.state) }

    public suspend fun submit(email: String, password: String) {
        store.send(LoginIntent.Submit(email, password))
    }

    public companion object {
        public fun create(): LoginComposeAdapter =
            LoginComposeAdapter(
                FlowStore.create(
                    initialState = LoginState.Idle,
                    reducer = LoginFlow(),
                    effects = LoginEffects()
                )
            )
    }
}
