package dev.intentflow.ai

public object FlowManifestYamlParser {
    private val listKeys = setOf(
        "states",
        "intents",
        "events",
        "effects",
        "routes",
        "outputs",
        "invariants",
        "acceptanceTraces"
    )

    public fun parse(text: String): FlowManifest {
        val scalars = mutableMapOf<String, String>()
        val lists = listKeys.associateWith { mutableListOf<String>() }
        var currentList: String? = null

        text.lineSequence()
            .map { it.substringBefore("#").trimEnd() }
            .filter { it.isNotBlank() }
            .forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("- ")) {
                    val key = currentList ?: error("List item without a list key: $line")
                    lists.getValue(key) += unquote(trimmed.removePrefix("- ").trim())
                    return@forEach
                }

                val key = trimmed.substringBefore(":", missingDelimiterValue = "").trim()
                val value = trimmed.substringAfter(":", missingDelimiterValue = "").trim()
                require(key.isNotBlank()) { "Invalid manifest line: $line" }

                if (key in listKeys && value.isBlank()) {
                    currentList = key
                } else {
                    currentList = null
                    scalars[key] = unquote(value)
                }
            }

        return FlowManifest(
            schemaVersion = scalars.getValue("schemaVersion"),
            feature = scalars.getValue("feature"),
            mode = FlowManifest.Mode.parse(scalars.getValue("mode")),
            summary = scalars["summary"],
            states = lists.getValue("states"),
            intents = lists.getValue("intents"),
            events = lists.getValue("events"),
            effects = lists.getValue("effects"),
            routes = lists.getValue("routes"),
            outputs = lists.getValue("outputs"),
            invariants = lists.getValue("invariants"),
            acceptanceTraces = lists.getValue("acceptanceTraces")
        )
    }

    private fun unquote(value: String): String =
        value.removeSurrounding("\"").removeSurrounding("'")
}
