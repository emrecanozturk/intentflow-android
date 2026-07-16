package dev.intentflow.examples.viewmodel

import dev.intentflow.core.EffectId
import dev.intentflow.core.EffectPolicy
import dev.intentflow.core.FlowEffectHandler
import dev.intentflow.core.FlowReducer
import dev.intentflow.core.FlowSignal
import dev.intentflow.core.FlowStore
import dev.intentflow.core.Next
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

public sealed interface UploadState {
    public data object Idle : UploadState
    public data class Uploading(public val fileName: String) : UploadState
    public data class Failed(public val reason: String) : UploadState
    public data object Finished : UploadState
}

public sealed interface UploadIntent {
    public data class Start(public val fileName: String) : UploadIntent
    public data object Retry : UploadIntent
    public data object Cancel : UploadIntent
}

public sealed interface UploadEvent {
    public data object Completed : UploadEvent
    public data class Failed(public val reason: String) : UploadEvent
}

public sealed interface UploadEffect {
    public data class Upload(public val fileName: String) : UploadEffect
}

public sealed interface UploadOutput {
    public data object Finished : UploadOutput
}

public sealed interface UploadRoute

public class UploadFlow :
    FlowReducer<UploadState, UploadIntent, UploadEvent, UploadEffect, UploadOutput, UploadRoute> {
    override fun reduce(
        state: UploadState,
        signal: FlowSignal<UploadIntent, UploadEvent>
    ): Next<UploadState, UploadEffect, UploadOutput, UploadRoute> =
        when (signal) {
            is FlowSignal.IntentSignal -> when (val intent = signal.intent) {
                is UploadIntent.Start ->
                    Next.state<UploadState, UploadEffect, UploadOutput, UploadRoute>(
                        UploadState.Uploading(intent.fileName)
                    ).effect(
                        UploadEffect.Upload(intent.fileName),
                        id = EffectId("upload.file"),
                        policy = EffectPolicy.CancelInFlight
                    )
                is UploadIntent.Retry ->
                    if (state is UploadState.Failed) {
                        Next.state<UploadState, UploadEffect, UploadOutput, UploadRoute>(
                            UploadState.Uploading("last-file")
                        ).effect(UploadEffect.Upload("last-file"), id = EffectId("upload.file"))
                    } else {
                        Next.state(state)
                    }
                is UploadIntent.Cancel ->
                    Next.state<UploadState, UploadEffect, UploadOutput, UploadRoute>(UploadState.Idle)
                        .cancel(EffectId("upload.file"))
            }
            is FlowSignal.EventSignal -> when (val event = signal.event) {
                is UploadEvent.Completed ->
                    Next.state<UploadState, UploadEffect, UploadOutput, UploadRoute>(
                        UploadState.Finished
                    ).output(UploadOutput.Finished)
                is UploadEvent.Failed -> Next.state(UploadState.Failed(event.reason))
            }
        }
}

public class UploadEffects : FlowEffectHandler<UploadEffect, UploadEvent> {
    override fun handle(effect: UploadEffect): Flow<UploadEvent> =
        flow {
            when (effect) {
                is UploadEffect.Upload -> emit(UploadEvent.Completed)
            }
        }
}

public class UploadViewModel(
    private val store: FlowStore<UploadState, UploadIntent, UploadEvent, UploadEffect, UploadOutput, UploadRoute>,
    private val scope: CoroutineScope
) {
    public val snapshots = store.snapshots

    public fun start(fileName: String) {
        scope.launch {
            store.send(UploadIntent.Start(fileName))
        }
    }

    public fun cancel() {
        scope.launch {
            store.send(UploadIntent.Cancel)
        }
    }
}
