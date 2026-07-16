package dev.intentflow.core

public data class FlowSnapshot<out State, out Output, out Route>(
    public val state: State,
    public val outputs: List<Output> = emptyList(),
    public val routes: List<Route> = emptyList()
)
