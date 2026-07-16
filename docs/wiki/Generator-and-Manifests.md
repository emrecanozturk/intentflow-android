# Generator and Manifests

IntentFlow includes a small CLI generator. It creates a starting point, not a finished product.

## Generate Core

```bash
./gradlew :intentflow-generator:run --args="feature Profile --mode core --ui none --output ./app/src/main/kotlin/features"
```

## Generate AI

```bash
./gradlew :intentflow-generator:run --args="feature Checkout --mode ai --ui compose --output ./app/src/main/kotlin/features"
```

## Generated Files

```text
Checkout/
  CheckoutContract.kt
  CheckoutFlow.kt
  CheckoutEffects.kt
  CheckoutProjection.kt
  CheckoutFlowTests.kt
  Checkout.intentflow.yaml
```

The manifest is generated only in AI mode.

## Manifest Shape

```yaml
schemaVersion: "0.1"
feature: "Checkout"
mode: "ai"
summary: "Collect payment and complete an order."
states:
  - idle
  - validatingCart
  - authorizingPayment
  - failed(message)
  - completed(orderID)
intents:
  - start
  - retry
  - cancel
events:
  - cartValid
  - paymentAuthorized(orderID)
  - failed(message)
effects:
  - validateCart
  - authorizePayment
routes:
  - paymentSheet
outputs:
  - orderCompleted(orderID)
invariants:
  - "Payment authorization can only start after cart validation succeeds."
acceptanceTraces:
  - "idle + start -> validatingCart + validateCart effect"
```

## Validate

```bash
./gradlew :intentflow-generator:run --args="validate .intentflow/login.intentflow.yaml"
```

Validation checks required manifest structure and AI-mode expectations.

## Generate AI Context

```bash
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool codex"
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool claude"
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool gemini"
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool copilot"
```

The output is a compact handoff for coding agents. It includes:

- feature summary
- states, intents, events, effects, routes, outputs
- invariants
- acceptance traces
- provider-specific files to read
- verification commands

## Generator Philosophy

The generator should:

- create the architectural skeleton
- make behavior visible
- include tests
- include cancellation IDs where useful
- include a manifest in AI mode
- keep output understandable

The generator should not:

- invent business decisions silently
- hide side effects in UI
- generate huge modules
- replace human review

## When To Edit The Manifest

Update the manifest when you add, remove, or rename:

- state
- intent
- event
- effect
- route
- output
- invariant
- acceptance trace

AI-generated changes should update manifest and tests together.
