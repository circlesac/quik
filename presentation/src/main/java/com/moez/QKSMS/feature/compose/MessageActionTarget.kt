package dev.octoshrimpy.quik.feature.compose

import android.view.View

data class MessageActionTarget(
    val anchor: View,
    val messageId: Long,
    val hasText: Boolean,
    val locked: Boolean,
)
