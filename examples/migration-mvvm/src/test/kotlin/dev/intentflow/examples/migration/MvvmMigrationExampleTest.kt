package dev.intentflow.examples.migration

import dev.intentflow.core.FlowSignal
import kotlin.test.Test
import kotlin.test.assertEquals

class MvvmMigrationExampleTest {
    @Test
    fun checkoutBehaviorMovesOutOfViewModel() {
        val next = CheckoutFlow().reduce(
            CheckoutState.Idle,
            FlowSignal.intent(CheckoutIntent.Submit)
        )

        assertEquals(CheckoutState.Authorizing, next.state)
        assertEquals(CheckoutEffect.Authorize, next.effects.single().effect)
    }
}
