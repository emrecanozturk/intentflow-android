package dev.intentflow.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public class FlowStore<State, Intent, Event, Effect, Output, Route>(
    initialState: State,
    private val reducer: FlowReducer<State, Intent, Event, Effect, Output, Route>,
    private val effects: FlowEffectHandler<Effect, Event>,
    private val scope: CoroutineScope
) {
    private val mutex = Mutex()
    private val runningEffects = mutableMapOf<EffectId, Job>()
    private val historyStorage = mutableListOf<FlowSnapshot<State, Output, Route>>()
    private var currentState: State = initialState

    private val snapshotState = MutableStateFlow(
        FlowSnapshot<State, Output, Route>(state = initialState)
    )

    public val snapshots: StateFlow<FlowSnapshot<State, Output, Route>> = snapshotState

    public val state: State
        get() = currentState

    public val history: List<FlowSnapshot<State, Output, Route>>
        get() = historyStorage.toList()

    public suspend fun send(intent: Intent): FlowSnapshot<State, Output, Route> =
        apply(FlowSignal.IntentSignal(intent))

    public suspend fun receive(event: Event): FlowSnapshot<State, Output, Route> =
        apply(FlowSignal.EventSignal(event))

    public suspend fun cancelEffect(id: EffectId) {
        mutex.withLock {
            runningEffects.remove(id)?.cancel()
        }
    }

    public suspend fun cancelAllEffects() {
        mutex.withLock {
            runningEffects.values.forEach { it.cancel() }
            runningEffects.clear()
        }
    }

    public fun close() {
        scope.cancel()
    }

    private suspend fun apply(
        signal: FlowSignal<Intent, Event>
    ): FlowSnapshot<State, Output, Route> {
        val next = mutex.withLock {
            val next = reducer.reduce(currentState, signal)
            currentState = next.state

            val snapshot = FlowSnapshot(
                state = next.state,
                outputs = next.outputs,
                routes = next.routes
            )
            historyStorage += snapshot
            snapshotState.value = snapshot
            next
        }

        next.effects.forEach { process(it) }
        return snapshotState.value
    }

    private suspend fun process(request: EffectRequest<Effect>) {
        val id = request.id
        if (id != null && request.policy == EffectPolicy.CancelOnly) {
            cancelEffect(id)
            return
        }

        if (id != null && request.policy == EffectPolicy.CancelInFlight) {
            cancelEffect(id)
        }

        val effect = request.effect ?: return
        val job = scope.launch {
            effects.handle(effect).collect { event ->
                receive(event)
            }
            if (id != null) {
                mutex.withLock {
                    runningEffects.remove(id)
                }
            }
        }

        if (id != null) {
            mutex.withLock {
                runningEffects[id] = job
            }
        }
    }

    public companion object {
        public fun <State, Intent, Event, Effect, Output, Route> create(
            initialState: State,
            reducer: FlowReducer<State, Intent, Event, Effect, Output, Route>,
            effects: FlowEffectHandler<Effect, Event>
        ): FlowStore<State, Intent, Event, Effect, Output, Route> =
            FlowStore(
                initialState = initialState,
                reducer = reducer,
                effects = effects,
                scope = CoroutineScope(SupervisorJob())
            )
    }
}
