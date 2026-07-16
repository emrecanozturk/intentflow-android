---
paths:
  - "intentflow-*/src/**/*.kt"
  - "examples/**/*.kt"
---

# IntentFlow Kotlin Rules

- Start from the manifest and contract before editing reducers or UI adapters.
- Keep reducers deterministic and side-effect free.
- Put async work in `FlowEffectHandler`.
- Return typed events from effects.
- Add or update reducer trace tests for new behavior.
- Keep Android lifecycle, dispatcher, and UI concerns outside reducers.
- Avoid retaining UI objects from effects; prefer value payloads and injected dependencies.
