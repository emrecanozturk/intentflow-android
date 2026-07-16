package dev.intentflow.examples.viewmodel

import dev.intentflow.core.FlowSignal
import kotlin.test.Test
import kotlin.test.assertEquals

class UploadViewModelExampleTest {
    @Test
    fun uploadStartCreatesCancellableEffect() {
        val next = UploadFlow().reduce(
            UploadState.Idle,
            FlowSignal.intent(UploadIntent.Start("avatar.png"))
        )

        assertEquals(UploadState.Uploading("avatar.png"), next.state)
        assertEquals(UploadEffect.Upload("avatar.png"), next.effects.single().effect)
    }
}
