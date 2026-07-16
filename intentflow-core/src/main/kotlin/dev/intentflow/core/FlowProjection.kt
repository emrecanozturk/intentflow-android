package dev.intentflow.core

public fun interface FlowProjection<State, ViewState> {
    public fun project(state: State): ViewState
}
