package dev.intentflow.core

public sealed interface FlowSignal<out Intent, out Event> {
    public data class IntentSignal<Intent>(public val intent: Intent) : FlowSignal<Intent, Nothing>
    public data class EventSignal<Event>(public val event: Event) : FlowSignal<Nothing, Event>

    public companion object {
        public fun <Intent> intent(intent: Intent): FlowSignal<Intent, Nothing> = IntentSignal(intent)
        public fun <Event> event(event: Event): FlowSignal<Nothing, Event> = EventSignal(event)
    }
}
