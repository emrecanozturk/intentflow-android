# Generator

IntentFlow includes a small generator CLI.

## Generate Core Feature

```bash
./gradlew :intentflow-generator:run --args="feature Profile --mode core --ui none --output ./app/src/main/kotlin/features"
```

## Generate AI Feature

```bash
./gradlew :intentflow-generator:run --args="feature Checkout --mode ai --ui compose --output ./app/src/main/kotlin/features"
```

## Validate Manifest

```bash
./gradlew :intentflow-generator:run --args="validate .intentflow/login.intentflow.yaml"
```

## Generate AI Context

```bash
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool codex"
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool claude"
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool gemini"
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool copilot"
```

The context output is a compact Markdown handoff for coding agents. It includes the manifest contract, invariants, acceptance traces, provider-specific instruction files, and verification commands.

## Output

```text
Checkout/
  CheckoutContract.kt
  CheckoutFlow.kt
  CheckoutEffects.kt
  CheckoutProjection.kt
  CheckoutFlowTests.kt
  Checkout.intentflow.yaml
```

## Philosophy

The generator intentionally creates a starting point, not a finished product.

It should:

- create the architectural skeleton
- include a reducer test
- include explicit loading and failure states
- include cancellation ID usage
- include AI manifest when requested
- produce compact AI context from a manifest

It should not:

- invent domain decisions silently
- hide side effects in UI
- generate a huge module the user cannot understand
