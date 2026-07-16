package dev.intentflow.ai

public data class AiGenerationPlan(
    public val featureName: String,
    public val mode: FlowManifest.Mode,
    public val files: List<String>,
    public val verificationCommands: List<String>
) {
    public companion object {
        public fun from(manifest: FlowManifest): AiGenerationPlan {
            val baseName = manifest.feature
            val files = buildList {
                add("${baseName}Contract.kt")
                add("${baseName}Flow.kt")
                add("${baseName}Effects.kt")
                add("${baseName}Projection.kt")
                add("${baseName}FlowTest.kt")
                if (manifest.mode == FlowManifest.Mode.Ai) {
                    add("${baseName}.intentflow.yaml")
                    add("${baseName}.ai-context.md")
                }
            }

            return AiGenerationPlan(
                featureName = baseName,
                mode = manifest.mode,
                files = files,
                verificationCommands = listOf(
                    "./gradlew test",
                    "./gradlew :intentflow-generator:run --args=\"validate .intentflow/${baseName.lowercase()}.intentflow.yaml\""
                )
            )
        }
    }
}
