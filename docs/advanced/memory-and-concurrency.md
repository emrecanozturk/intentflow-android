# Memory and Concurrency

IntentFlow is designed for async Android workflows without hiding lifetime decisions.

## Store Lifetime

`FlowStore` is a coroutine-backed store. It owns:

- current state
- running effect tasks
- snapshot history
- observers

Effects are started as tasks and are cancelled by `EffectId`.

## Avoiding Retain Cycles

The store does not require UI adapters to retain it forever.

Recommended Jetpack Compose pattern:

```kotlin
deinit {
    observation?.cancel()
    bindTask?.cancel()
}
```

Recommended Android Views pattern:

```kotlin
deinit {
    observation?.cancel()
    bindTask?.cancel()
}
```

When an adapter starts a task, capture `self` weakly:

```kotlin
Task { [weak self] in
    guard let self else { return }
    await store.send(.appear)
}
```

When an effect wraps `AsyncStream`, cancel internal work on termination:

```kotlin
continuation.onTermination = { _ in
    task.cancel()
}
```

## Cancellation Rule

Any effect that can outlive the user intent should have an ID:

```kotlin
.effect(.upload(payload), id: "upload.stream", policy: .cancelInFlight)
```

Then pause, cancel, retry, and deinit flows can stop it deterministically:

```kotlin
.cancel("upload.stream")
```

## Reducer Rule

Reducers should not contain `Task`, `await`, network clients, database clients, timers, or UI objects.

This is what keeps them memory-safe and testable.

## Adapter Rule

UI adapters may be platform-specific. They are allowed to:

- own labels, views, and Jetpack Compose bindings
- bind to snapshots
- send intents
- interpret routes

They should not:

- decide product state transitions
- call APIs directly
- store workflow-only booleans such as `isLoadingPayment` when that is already state
