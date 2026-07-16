---
applyTo: "intentflow-*/src/**/*.kt,examples/**/*.kt"
---

# IntentFlow Kotlin Instructions

- Model product behavior first: state, intent, event, effect, route, output.
- Keep reducers pure and put side effects in handlers.
- Use `EffectId` for repeatable, cancellable, or replaceable async work.
- Keep Jetpack Compose views and Android View adapters as adapters over projections.
- Generated code is incomplete without tests for the transition it introduces.
- If an AI-mode feature changes, update the `.intentflow.yaml` manifest before or with the Kotlin contract.
