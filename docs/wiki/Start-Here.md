# Start Here

This page is the shortest useful path through IntentFlow.

## 1. Verify The Package

```bash
git clone https://github.com/emrecanozturk/intentflow-android.git
cd intentflow-android
./gradlew test
./gradlew :intentflow-generator:run --args="validate .intentflow/login.intentflow.yaml"
```

For the full local release check:

```bash
./scripts/check.sh
```

## 2. Learn The Sentence

IntentFlow models a feature as:

```text
State + Intent/Event -> Next State + Effects + Outputs + Routes
```

If a feature cannot be explained through that sentence, the workflow contract is probably not clear enough yet.

## 3. Pick A Feature

Good first candidates:

- login with two-factor recovery
- checkout with payment retry
- upload with progress and cancellation
- permission request with fallback
- device connection with trust and recovery
- onboarding with branching routes

Avoid starting with a simple static screen. IntentFlow is most useful when behavior is doing real work.

## 4. Draw The Contract

Before writing UI, list:

- states
- user intents
- external events
- effect requests
- routes
- parent outputs
- invariants
- acceptance traces

Example:

```text
idle + submit -> validating + validateCredentials effect
validating + credentialsValid -> requestingToken + requestToken effect
requestingToken + tokenRequiresTwoFactor -> waitingForTwoFactor
requestingToken + tokenReceived -> authenticated + completed output
```

## 5. Generate A Starting Point

Core mode:

```bash
./gradlew :intentflow-generator:run --args="feature Profile --mode core --ui none --output ./app/src/main/kotlin/features"
```

AI mode:

```bash
./gradlew :intentflow-generator:run --args="feature Checkout --mode ai --ui compose --output ./app/src/main/kotlin/features"
```

## 6. Write Behavior Tests First

The reducer is pure. Test transitions before wiring UI:

```kotlin
let trace = LoginFlow().trace(
    initialState: .idle,
    signals: [
        .intent(.submit(email: "user@example.com", password: "secret")),
        .event(.credentialsValid)
    ]
)
```

## 7. Add UI Last

Jetpack Compose views and Android View adapters should adapt to projected state and send intents. They should not own workflow rules.

## 8. Use AI Safely

For AI mode, generate a compact context file:

```bash
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool codex"
```

Give the agent that output plus a small task. Do not ask an agent to infer the whole architecture from the repository.
