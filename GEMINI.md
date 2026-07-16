# Gemini CLI Instructions

IntentFlow is a workflow-first Android architecture. The smallest useful context is the manifest plus the files being edited.

## Context Loading

- Read `.ai/agent-context.md` first.
- For AI mode, read the relevant `.intentflow.yaml` file before Kotlin code.
- Use `.geminiignore` to avoid build products, Git internals, screenshots, and Android Studio user state.
- Prefer `./gradlew :intentflow-generator:run --args="ai-context <manifest> --tool gemini"` when asking Gemini for generation, review, or migration help.

## Architecture Rules

- Reducers are pure and return `Next`.
- Effects live in `FlowEffectHandler`.
- Jetpack Compose and Android Views files are adapters.
- Navigation is a route. Parent communication is an output.
- New behavior requires transition tests and manifest updates.

## Verification

```bash
./gradlew test
./gradlew :intentflow-generator:run --args="validate .intentflow/login.intentflow.yaml"
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool gemini"
```
