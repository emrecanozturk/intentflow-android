# IntentFlow Agent Context

IntentFlow is a Kotlin package that proposes a workflow-first architecture for Android apps. The key idea is:

```text
State + Intent/Event -> Next State + Effects + Outputs + Routes
```

## Repository Map

- `intentflow-core/src/main/kotlin`: core runtime, reducer/store/effect/projection/trace types.
- `intentflow-ai/src/main/kotlin`: manifest model, parser, validator, AI generation plan.
- `intentflow-generator/src/main/kotlin`: CLI for feature generation, manifest validation, and AI context rendering.
- `intentflow-core/src/test/kotlin`: runtime behavior tests.
- `intentflow-ai/src/test/kotlin`: manifest parser and validator tests.
- `intentflow-generator/src/test/kotlin`: generator and CLI support tests.
- `examples/compose-example`: Compose-style adapter example.
- `examples/viewmodel-example`: ViewModel adapter example.
- `examples/migration-mvvm`: migration example for moving behavior out of a ViewModel.
- `docs/ai`: AI-mode guidance, token budgeting, and agent usage.
- `.intentflow`: schema and sample manifests.
- `.github`, `.claude`, `.cursor`, `AGENTS.md`, `CLAUDE.md`, `GEMINI.md`: agent instruction surfaces.

## First Files For AI Work

1. `AGENTS.md`
2. `.ai/agent-context.md`
3. The relevant `.intentflow/*.intentflow.yaml` manifest.
4. The matching contract, reducer, effect handler, projection, and tests.

## Invariants

- Reducers are pure.
- Effects are typed and cancellable.
- UI is an adapter, not the workflow owner.
- Routes and outputs are explicit.
- AI-generated changes are incomplete without tests.

## Useful Commands

```bash
./gradlew test
./gradlew :intentflow-generator:run --args="validate .intentflow/login.intentflow.yaml"
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool codex"
./scripts/check.sh
```
