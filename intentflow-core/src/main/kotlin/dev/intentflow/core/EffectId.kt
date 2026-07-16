package dev.intentflow.core

@JvmInline
public value class EffectId(public val rawValue: String) {
    override fun toString(): String = rawValue
}
