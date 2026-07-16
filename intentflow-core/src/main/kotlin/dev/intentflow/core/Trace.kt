package dev.intentflow.core

public data class FlowTraceStep<out State, out Effect, out Output, out Route>(
    public val state: State,
    public val effects: List<EffectRequest<Effect>>,
    public val outputs: List<Output>,
    public val routes: List<Route>
) {
    public constructor(next: Next<State, Effect, Output, Route>) : this(
        state = next.state,
        effects = next.effects,
        outputs = next.outputs,
        routes = next.routes
    )
}

public data class FlowTrace<out State, out Effect, out Output, out Route>(
    public val steps: List<FlowTraceStep<State, Effect, Output, Route>>
)

public fun <State, Intent, Event, Effect, Output, Route>
    FlowReducer<State, Intent, Event, Effect, Output, Route>.trace(
    initialState: State,
    signals: List<FlowSignal<Intent, Event>>
): FlowTrace<State, Effect, Output, Route> {
    var state = initialState
    val steps = mutableListOf<FlowTraceStep<State, Effect, Output, Route>>()

    for (signal in signals) {
        val next = reduce(state, signal)
        state = next.state
        steps += FlowTraceStep(next)
    }

    return FlowTrace(steps)
}
