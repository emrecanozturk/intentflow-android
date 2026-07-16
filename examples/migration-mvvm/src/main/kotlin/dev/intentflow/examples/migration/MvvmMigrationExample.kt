package dev.intentflow.examples.migration

import dev.intentflow.core.FlowReducer
import dev.intentflow.core.FlowSignal
import dev.intentflow.core.Next

public class LegacyCheckoutViewModel {
    public var isLoading: Boolean = false
        private set
    public var errorMessage: String? = null
        private set

    public fun submit() {
        isLoading = true
    }

    public fun fail(message: String) {
        isLoading = false
        errorMessage = message
    }
}

public sealed interface CheckoutState {
    public data object Idle : CheckoutState
    public data object Authorizing : CheckoutState
    public data class Failed(public val message: String) : CheckoutState
    public data object Complete : CheckoutState
}

public sealed interface CheckoutIntent {
    public data object Submit : CheckoutIntent
}

public sealed interface CheckoutEvent {
    public data object Authorized : CheckoutEvent
    public data class Failed(public val message: String) : CheckoutEvent
}

public sealed interface CheckoutEffect {
    public data object Authorize : CheckoutEffect
}

public sealed interface CheckoutOutput {
    public data object Completed : CheckoutOutput
}

public sealed interface CheckoutRoute

public class CheckoutFlow :
    FlowReducer<CheckoutState, CheckoutIntent, CheckoutEvent, CheckoutEffect, CheckoutOutput, CheckoutRoute> {
    override fun reduce(
        state: CheckoutState,
        signal: FlowSignal<CheckoutIntent, CheckoutEvent>
    ): Next<CheckoutState, CheckoutEffect, CheckoutOutput, CheckoutRoute> =
        when (signal) {
            is FlowSignal.IntentSignal ->
                if (state is CheckoutState.Idle && signal.intent is CheckoutIntent.Submit) {
                    Next.state<CheckoutState, CheckoutEffect, CheckoutOutput, CheckoutRoute>(
                        CheckoutState.Authorizing
                    ).effect(CheckoutEffect.Authorize)
                } else {
                    Next.state(state)
                }
            is FlowSignal.EventSignal -> when (val event = signal.event) {
                CheckoutEvent.Authorized ->
                    Next.state<CheckoutState, CheckoutEffect, CheckoutOutput, CheckoutRoute>(
                        CheckoutState.Complete
                    ).output(CheckoutOutput.Completed)
                is CheckoutEvent.Failed -> Next.state(CheckoutState.Failed(event.message))
            }
        }
}
