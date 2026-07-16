package dev.intentflow.generator

import dev.intentflow.ai.FlowManifest

public enum class AiTool(public val id: String) {
    Codex("codex"),
    Claude("claude"),
    Gemini("gemini"),
    Copilot("copilot"),
    Cursor("cursor"),
    Generic("generic");

    public companion object {
        public fun parse(value: String): AiTool? =
            entries.firstOrNull { it.id == value.lowercase() }
    }
}

public object AiContextTemplate {
    public fun render(manifest: FlowManifest, tool: AiTool): String =
        buildString {
            appendLine("# IntentFlow AI Context: ${manifest.feature}")
            appendLine()
            appendLine("Tool: ${tool.id}")
            appendLine("Mode: ${manifest.mode.name.lowercase()}")
            appendLine()
            appendLine("## Contract")
            appendLine()
            appendLine("- Feature: ${manifest.feature}")
            manifest.summary?.let { appendLine("- Summary: $it") }
            appendLine("- States: ${manifest.states.joinToString()}")
            appendLine("- Intents: ${manifest.intents.joinToString()}")
            appendLine("- Events: ${manifest.events.joinToString()}")
            appendLine("- Effects: ${manifest.effects.joinToString()}")
            appendLine("- Outputs: ${manifest.outputs.joinToString()}")
            appendLine("- Routes: ${manifest.routes.joinToString()}")
            appendLine()
            appendLine("## Rules")
            appendLine()
            appendLine("- Keep reducers pure. Do not call repositories, Android APIs, clocks, or network from a reducer.")
            appendLine("- Emit typed effects for async work. Effects return events.")
            appendLine("- Emit typed routes for navigation. UI adapters decide how to navigate.")
            appendLine("- Emit typed outputs for parent communication.")
            appendLine("- Add tests for every new workflow transition before changing adapters.")
            appendLine("- Keep generated files scoped to this feature unless a manifest says otherwise.")
            appendLine()
            if (manifest.invariants.isNotEmpty()) {
                appendLine("## Invariants")
                appendLine()
                manifest.invariants.forEach { appendLine("- $it") }
                appendLine()
            }
            if (manifest.acceptanceTraces.isNotEmpty()) {
                appendLine("## Acceptance Traces")
                appendLine()
                manifest.acceptanceTraces.forEach { appendLine("- $it") }
            }
        }
}
