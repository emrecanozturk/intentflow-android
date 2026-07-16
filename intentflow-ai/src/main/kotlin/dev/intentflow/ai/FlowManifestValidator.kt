package dev.intentflow.ai

public data class ValidationResult(
    public val errors: List<String>
) {
    public val isValid: Boolean
        get() = errors.isEmpty()
}

public object FlowManifestValidator {
    public fun validate(manifest: FlowManifest): ValidationResult {
        val errors = mutableListOf<String>()

        if (manifest.schemaVersion.isBlank()) errors += "schemaVersion is required"
        if (manifest.feature.isBlank()) errors += "feature is required"
        if (manifest.states.isEmpty()) errors += "states must not be empty"
        if (manifest.intents.isEmpty()) errors += "intents must not be empty"
        if (manifest.effects.isEmpty()) errors += "effects must not be empty"

        if (manifest.mode == FlowManifest.Mode.Ai) {
            if (manifest.invariants.isEmpty()) {
                errors += "AI mode requires at least one invariant"
            }
            if (manifest.acceptanceTraces.isEmpty()) {
                errors += "AI mode requires at least one acceptance trace"
            }
        }

        return ValidationResult(errors)
    }
}
