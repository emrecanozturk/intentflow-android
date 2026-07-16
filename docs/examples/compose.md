# Jetpack Compose Example Notes

The Jetpack Compose example models a device connection session.

It is intentionally not a simple counter because simple examples hide the reason this architecture exists.

The flow includes:

- scanning
- choosing a device
- connecting
- waiting for trust
- verifying internet
- ready session
- recoverable failure

The Jetpack Compose view never decides what the feature means. It renders `DeviceConnectionViewState` and sends `DeviceConnectionIntent`.

Read the example: [Compose adapter example](../../examples/compose-example)
