package dev.intentflow.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class FlowStoreTest {
    @Test
    fun storeRunsEffectsBackIntoEvents() = runTest {
        val testScope = TestScope(StandardTestDispatcher(testScheduler))
        val store = FlowStore(
            initialState = LoginState.Idle,
            reducer = LoginFlow(),
            effects = LoginEffects(),
            scope = testScope
        )

        store.send(LoginIntent.Submit("user@example.com", "secret"))
        advanceUntilIdle()

        assertEquals(LoginState.Authenticated("user-1"), store.state)
        assertTrue(store.history.any { it.outputs == listOf(LoginOutput.Completed("user-1")) })
    }

    @Test
    fun snapshotsRecordStateOutputsAndRoutes() = runTest {
        val store = FlowStore.create(
            initialState = LoginState.Idle,
            reducer = LoginFlow(),
            effects = NoEffectHandler<LoginEffect, LoginEvent>()
        )

        store.send(LoginIntent.Submit("user@example.com", "secret"))
        store.receive(LoginEvent.CredentialsValid)
        store.receive(LoginEvent.TokenRequiresTwoFactor)

        assertEquals(LoginState.WaitingForTwoFactor, store.snapshots.value.state)
        assertEquals(listOf(LoginRoute.TwoFactor), store.snapshots.value.routes)
        assertEquals(3, store.history.size)
    }
}
