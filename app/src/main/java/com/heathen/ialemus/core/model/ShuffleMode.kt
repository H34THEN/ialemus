package com.heathen.ialemus.core.model

enum class ShuffleMode(val displayName: String) {
    PURE_CHAOS("Pure Chaos"),
    SMART_CHAOS("Smart Chaos"),
    DEEP_CUT_RITUAL("Deep Cut Ritual"),
    FAVORITE_STORM("Favorite Storm"),
    ;

    companion object {
        val DEFAULT = PURE_CHAOS
    }
}
