package dev.intentflow.ai

import kotlin.test.Test
import kotlin.test.assertEquals

internal class FlowManifestYamlParserTest {
    @Test
    fun parsesManifest() {
        val manifest = FlowManifestYamlParser.parse(
            """
            schemaVersion: "0.1"
            feature: "Login"
            mode: "ai"
            summary: "Authenticate a user."
            states:
              - idle
              - validating
              - authenticated(userId)
            intents:
              - submit(email,password)
            events:
              - credentialsValid
            effects:
              - validateCredentials
            routes:
              - twoFactor
            outputs:
              - completed(userId)
            invariants:
              - "Token request only starts after credentials are valid."
            acceptanceTraces:
              - "idle + submit -> validating + validateCredentials effect"
            """.trimIndent()
        )

        assertEquals("Login", manifest.feature)
        assertEquals(FlowManifest.Mode.Ai, manifest.mode)
        assertEquals(listOf("idle", "validating", "authenticated(userId)"), manifest.states)
    }
}
