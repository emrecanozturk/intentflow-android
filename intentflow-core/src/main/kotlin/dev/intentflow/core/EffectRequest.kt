package dev.intentflow.core

public data class EffectRequest<out Effect>(
    public val effect: Effect?,
    public val id: EffectId? = null,
    public val policy: EffectPolicy = EffectPolicy.Run
) {
    public companion object {
        public fun <Effect> run(
            effect: Effect,
            id: EffectId? = null,
            policy: EffectPolicy = EffectPolicy.Run
        ): EffectRequest<Effect> = EffectRequest(effect = effect, id = id, policy = policy)

        public fun <Effect> cancel(id: EffectId): EffectRequest<Effect> =
            EffectRequest(effect = null, id = id, policy = EffectPolicy.CancelOnly)
    }
}
