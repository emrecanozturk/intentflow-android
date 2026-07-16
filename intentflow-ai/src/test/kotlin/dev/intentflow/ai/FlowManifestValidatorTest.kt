package dev.intentflow.ai

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class FlowManifestValidatorTest {
    @Test
    fun aiModeRequiresInvariantsAndAcceptanceTraces() {
        val manifest = FlowManifest(
            schemaVersion = "0.1",
            feature = "Checkout",
            mode = FlowManifest.Mode.Ai,
            summary = null,
            states = listOf("idle"),
            intents = listOf("start"),
            events = emptyList(),
            effects = listOf("validateCart"),
            routes = emptyList(),
            outputs = emptyList(),
            invariants = emptyList(),
            acceptanceTraces = emptyList()
        )

        val result = FlowManifestValidator.validate(manifest)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("invariant") })
        assertTrue(result.errors.any { it.contains("acceptance trace") })
    }

    @Test
    fun planAddsAiFilesOnlyForAiMode() {
        val manifest = FlowManifest(
            schemaVersion = "0.1",
            feature = "Checkout",
            mode = FlowManifest.Mode.Ai,
            summary = null,
            states = listOf("idle"),
            intents = listOf("start"),
            events = emptyList(),
            effects = listOf("validateCart"),
            routes = emptyList(),
            outputs = emptyList(),
            invariants = listOf("Cart must be valid before payment."),
            acceptanceTraces = listOf("idle + start -> validatingCart")
        )

        val plan = AiGenerationPlan.from(manifest)

        assertTrue(plan.files.contains("Checkout.intentflow.yaml"))
        assertTrue(FlowManifestValidator.validate(manifest).isValid)
    }
}
