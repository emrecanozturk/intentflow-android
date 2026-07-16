# Android Views Example Notes

The Android Views example models a file upload workflow.

It includes:

- document selection route
- prepare effect
- upload stream effect
- progress events
- pause
- retry
- cancellation
- completion output

The view controller remains imperative because Android Views is imperative. IntentFlow does not try to hide that. The important rule is that Android Views adapts to behavior; it does not own behavior.

Read the example: [ViewModel adapter example](../../examples/viewmodel-example)
