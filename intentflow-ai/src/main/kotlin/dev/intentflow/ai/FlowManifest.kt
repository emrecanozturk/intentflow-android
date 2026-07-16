package dev.intentflow.ai

public data class FlowManifest(
    public val schemaVersion: String,
    public val feature: String,
    public val mode: Mode,
    public val summary: String?,
    public val states: List<String>,
    public val intents: List<String>,
    public val events: List<String>,
    public val effects: List<String>,
    public val routes: List<String>,
    public val outputs: List<String>,
    public val invariants: List<String>,
    public val acceptanceTraces: List<String>
) {
    public enum class Mode {
        Core,
        Ai;

        public companion object {
            public fun parse(value: String): Mode =
                when (value.trim().lowercase()) {
                    "core" -> Core
                    "ai" -> Ai
                    else -> error("Unsupported manifest mode: $value")
                }
        }
    }
}
