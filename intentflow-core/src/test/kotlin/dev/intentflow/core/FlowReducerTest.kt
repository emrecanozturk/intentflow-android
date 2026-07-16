package dev.intentflow.core

import kotlin.test.Test
import kotlin.test.assertEquals

internal class FlowReducerTest {
    @Test
    fun traceKeepsBehaviorVisibleWithoutUi() {
        val trace = LoginFlow().trace(
            initialState = LoginState.Idle,
            signals = listOf(
                FlowSignal.intent(LoginIntent.Submit("user@example.com", "secret")),
                FlowSignal.event(LoginEvent.CredentialsValid),
                FlowSignal.event(LoginEvent.TokenRequiresTwoFactor)
            )
        )

        assertEquals(
            listOf(
                LoginState.Validating("user@example.com"),
                LoginState.RequestingToken,
                LoginState.WaitingForTwoFactor
            ),
            trace.steps.map { it.state }
        )
        assertEquals(listOf(LoginRoute.TwoFactor), trace.steps.last().routes)
    }

    @Test
    fun cancelIntentCancelsKnownEffectsAndEmitsOutput() {
        val next = LoginFlow().reduce(
            state = LoginState.RequestingToken,
            signal = FlowSignal.intent(LoginIntent.Cancel)
        )

        assertEquals(LoginState.Idle, next.state)
        assertEquals(
            listOf(
                EffectRequest.cancel<LoginEffect>(EffectId("login.validate")),
                EffectRequest.cancel<LoginEffect>(EffectId("login.token"))
            ),
            next.effects
        )
        assertEquals(listOf(LoginOutput.Cancelled), next.outputs)
    }
}
