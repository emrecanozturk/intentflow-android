package dev.intentflow.core

public fun interface FlowReducer<State, Intent, Event, Effect, Output, Route> {
    public fun reduce(
        state: State,
        signal: FlowSignal<Intent, Event>
    ): Next<State, Effect, Output, Route>
}
