package dev.intentflow.generator

import dev.intentflow.ai.FlowManifest

public enum class UiKind(public val id: String) {
    Compose("compose"),
    ViewModel("viewmodel"),
    None("none");

    public companion object {
        public fun parse(value: String): UiKind? =
            entries.firstOrNull { it.id == value.lowercase() }
    }
}

public class FeatureTemplate(
    private val name: String,
    private val mode: FlowManifest.Mode,
    private val ui: UiKind
) {
    private val packageName: String = "features.${name.lowercase()}"

    public val contract: String
        get() = """
            package $packageName

            import dev.intentflow.core.EffectId

            public sealed interface ${name}State {
                public data object Idle : ${name}State
                public data object Loading : ${name}State
                public data class Failed(public val message: String) : ${name}State
                public data object Ready : ${name}State
            }

            public sealed interface ${name}Intent {
                public data object Start : ${name}Intent
                public data object Retry : ${name}Intent
                public data object DismissError : ${name}Intent
            }

            public sealed interface ${name}Event {
                public data object Loaded : ${name}Event
                public data class Failed(public val message: String) : ${name}Event
            }

            public sealed interface ${name}Effect {
                public data object Load : ${name}Effect
            }

            public sealed interface ${name}Output {
                public data object Completed : ${name}Output
            }

            public sealed interface ${name}Route {
                public data object Details : ${name}Route
            }

            public val ${name.replaceFirstChar { it.lowercase() }}LoadEffectId: EffectId =
                EffectId("$packageName.load")
        """.trimIndent()

    public val flow: String
        get() = """
            package $packageName

            import dev.intentflow.core.EffectPolicy
            import dev.intentflow.core.FlowReducer
            import dev.intentflow.core.FlowSignal
            import dev.intentflow.core.Next

            public class ${name}Flow :
                FlowReducer<${name}State, ${name}Intent, ${name}Event, ${name}Effect, ${name}Output, ${name}Route> {
                override fun reduce(
                    state: ${name}State,
                    signal: FlowSignal<${name}Intent, ${name}Event>
                ): Next<${name}State, ${name}Effect, ${name}Output, ${name}Route> =
                    when (signal) {
                        is FlowSignal.IntentSignal -> reduceIntent(state, signal.intent)
                        is FlowSignal.EventSignal -> reduceEvent(state, signal.event)
                    }

                private fun reduceIntent(
                    state: ${name}State,
                    intent: ${name}Intent
                ): Next<${name}State, ${name}Effect, ${name}Output, ${name}Route> =
                    when {
                        state is ${name}State.Idle && intent is ${name}Intent.Start ->
                            Next.state<${name}State, ${name}Effect, ${name}Output, ${name}Route>(
                                ${name}State.Loading
                            ).effect(
                                ${name}Effect.Load,
                                id = ${name.replaceFirstChar { it.lowercase() }}LoadEffectId,
                                policy = EffectPolicy.CancelInFlight
                            )

                        state is ${name}State.Failed && intent is ${name}Intent.Retry ->
                            Next.state<${name}State, ${name}Effect, ${name}Output, ${name}Route>(
                                ${name}State.Loading
                            ).effect(
                                ${name}Effect.Load,
                                id = ${name.replaceFirstChar { it.lowercase() }}LoadEffectId,
                                policy = EffectPolicy.CancelInFlight
                            )

                        state is ${name}State.Failed && intent is ${name}Intent.DismissError ->
                            Next.state(${name}State.Idle)

                        else -> Next.state(state)
                    }

                private fun reduceEvent(
                    state: ${name}State,
                    event: ${name}Event
                ): Next<${name}State, ${name}Effect, ${name}Output, ${name}Route> =
                    when {
                        state is ${name}State.Loading && event is ${name}Event.Loaded ->
                            Next.state<${name}State, ${name}Effect, ${name}Output, ${name}Route>(
                                ${name}State.Ready
                            ).output(${name}Output.Completed)

                        state is ${name}State.Loading && event is ${name}Event.Failed ->
                            Next.state(${name}State.Failed(event.message))

                        else -> Next.state(state)
                    }
            }
        """.trimIndent()

    public val effects: String
        get() = """
            package $packageName

            import dev.intentflow.core.FlowEffectHandler
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flow

            public class ${name}Effects : FlowEffectHandler<${name}Effect, ${name}Event> {
                override fun handle(effect: ${name}Effect): Flow<${name}Event> =
                    flow {
                        when (effect) {
                            is ${name}Effect.Load -> emit(${name}Event.Loaded)
                        }
                    }
            }
        """.trimIndent()

    public val projection: String
        get() = """
            package $packageName

            import dev.intentflow.core.FlowProjection

            public data class ${name}ViewState(
                public val title: String,
                public val isLoading: Boolean,
                public val errorMessage: String?
            )

            public class ${name}Projection : FlowProjection<${name}State, ${name}ViewState> {
                override fun project(state: ${name}State): ${name}ViewState =
                    when (state) {
                        is ${name}State.Idle -> ${name}ViewState("$name", isLoading = false, errorMessage = null)
                        is ${name}State.Loading -> ${name}ViewState("Loading", isLoading = true, errorMessage = null)
                        is ${name}State.Failed -> ${name}ViewState("Try again", isLoading = false, errorMessage = state.message)
                        is ${name}State.Ready -> ${name}ViewState("Ready", isLoading = false, errorMessage = null)
                    }
            }
        """.trimIndent()

    public val adapter: String?
        get() = when (ui) {
            UiKind.None -> null
            UiKind.Compose -> """
                package $packageName

                import dev.intentflow.core.FlowStore
                import kotlinx.coroutines.flow.map

                public class ${name}ComposeAdapter(
                    private val store: FlowStore<${name}State, ${name}Intent, ${name}Event, ${name}Effect, ${name}Output, ${name}Route>,
                    private val projection: ${name}Projection = ${name}Projection()
                ) {
                    public val viewState = store.snapshots.map { projection.project(it.state) }

                    public suspend fun onStartClicked() {
                        store.send(${name}Intent.Start)
                    }
                }
            """.trimIndent()

            UiKind.ViewModel -> """
                package $packageName

                import dev.intentflow.core.FlowStore
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.launch

                public class ${name}ViewModelAdapter(
                    private val store: FlowStore<${name}State, ${name}Intent, ${name}Event, ${name}Effect, ${name}Output, ${name}Route>,
                    private val scope: CoroutineScope
                ) {
                    public val snapshots = store.snapshots

                    public fun onStartClicked() {
                        scope.launch {
                            store.send(${name}Intent.Start)
                        }
                    }
                }
            """.trimIndent()
        }

    public val tests: String
        get() = """
            package $packageName

            import dev.intentflow.core.EffectPolicy
            import dev.intentflow.core.FlowSignal
            import kotlin.test.Test
            import kotlin.test.assertEquals

            class ${name}FlowTest {
                @Test
                fun startRunsLoadEffect() {
                    val flow = ${name}Flow()

                    val next = flow.reduce(${name}State.Idle, FlowSignal.intent(${name}Intent.Start))

                    assertEquals(${name}State.Loading, next.state)
                    assertEquals(${name}Effect.Load, next.effects.single().effect)
                    assertEquals(EffectPolicy.CancelInFlight, next.effects.single().policy)
                }
            }
        """.trimIndent()

    public val manifest: String
        get() = """
            schemaVersion: "0.1"
            feature: "$name"
            mode: "${mode.name.lowercase()}"
            summary: "$name generated by IntentFlow Android."
            states:
              - idle
              - loading
              - failed(message)
              - ready
            intents:
              - start
              - retry
              - dismissError
            events:
              - loaded
              - failed(message)
            effects:
              - load
            routes:
              - details
            outputs:
              - completed
            invariants:
              - "Loading can only be entered by start or retry."
              - "A failed state must keep a user-readable message."
            acceptanceTraces:
              - "idle + start -> loading + load effect"
              - "loading + loaded -> ready + completed output"
        """.trimIndent()
}
