package dev.intentflow.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

public fun interface FlowEffectHandler<Effect, Event> {
    public fun handle(effect: Effect): Flow<Event>
}

public class NoEffectHandler<Effect, Event> : FlowEffectHandler<Effect, Event> {
    override fun handle(effect: Effect): Flow<Event> = emptyFlow()
}
