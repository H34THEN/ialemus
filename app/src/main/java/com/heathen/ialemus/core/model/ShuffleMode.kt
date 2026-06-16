package com.heathen.ialemus.core.model

enum class ShuffleMode(val displayName: String) {
    OFF("Off"),
    TRUE_RANDOM("True Random"),
    REPEAT_QUEUE("Repeat Queue"),
    REPEAT_ONE("Repeat One"),
    ;

    companion object {
        val DEFAULT = OFF
    }
}
