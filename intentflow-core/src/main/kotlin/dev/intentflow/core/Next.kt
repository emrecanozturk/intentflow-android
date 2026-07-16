package dev.intentflow.core

public data class Next<out State, out Effect, out Output, out Route>(
    public val state: State,
    public val effects: List<EffectRequest<Effect>> = emptyList(),
    public val outputs: List<Output> = emptyList(),
    public val routes: List<Route> = emptyList()
) {
    public fun effect(
        effect: @UnsafeVariance Effect,
        id: EffectId? = null,
        policy: EffectPolicy = EffectPolicy.Run
    ): Next<State, Effect, Output, Route> =
        copy(effects = effects + EffectRequest.run(effect, id, policy))

    public fun cancel(id: EffectId): Next<State, Effect, Output, Route> =
        copy(effects = effects + EffectRequest.cancel(id))

    public fun output(output: @UnsafeVariance Output): Next<State, Effect, Output, Route> =
        copy(outputs = outputs + output)

    public fun route(route: @UnsafeVariance Route): Next<State, Effect, Output, Route> =
        copy(routes = routes + route)

    public companion object {
        public fun <State, Effect, Output, Route> state(
            state: State
        ): Next<State, Effect, Output, Route> = Next(state = state)
    }
}
