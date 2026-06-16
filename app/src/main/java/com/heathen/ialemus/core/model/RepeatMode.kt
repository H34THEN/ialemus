package com.heathen.ialemus.core.model

enum class RepeatMode(val displayName: String) {
    OFF("Off"),
    QUEUE("Repeat Queue"),
    ONE("Repeat One"),
    ;

    fun next(): RepeatMode = when (this) {
        OFF -> QUEUE
        QUEUE -> ONE
        ONE -> OFF
    }

    companion object {
        val DEFAULT = OFF
    }
}
