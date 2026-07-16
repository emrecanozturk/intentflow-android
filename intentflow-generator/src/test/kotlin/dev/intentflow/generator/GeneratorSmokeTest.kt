package dev.intentflow.generator

import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertTrue

class GeneratorSmokeTest {
    @Test
    fun generatesAiFeature() {
        val directory = Files.createTempDirectory("intentflow-generator-test")

        IntentFlowGenerate.run(
            listOf(
                "feature",
                "Checkout",
                "--mode",
                "ai",
                "--ui",
                "compose",
                "--output",
                directory.toString()
            )
        )

        assertTrue(directory.resolve("Checkout/CheckoutContract.kt").exists())
        assertTrue(directory.resolve("Checkout/CheckoutAdapter.kt").exists())
        assertTrue(directory.resolve("Checkout/Checkout.intentflow.yaml").exists())
        assertTrue(directory.resolve("Checkout/Checkout.ai-context.md").exists())
    }
}
