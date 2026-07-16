package dev.intentflow.examples.compose

import dev.intentflow.core.FlowSignal
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginComposeExampleTest {
    @Test
    fun reducerKeepsComposeAdapterThin() {
        val next = LoginFlow().reduce(
            LoginState.Idle,
            FlowSignal.intent(LoginIntent.Submit("emre@example.com", "password"))
        )

        assertEquals(LoginState.Validating("emre@example.com"), next.state)
        assertEquals(LoginEffect.Validate("emre@example.com", "password"), next.effects.single().effect)
    }
}
