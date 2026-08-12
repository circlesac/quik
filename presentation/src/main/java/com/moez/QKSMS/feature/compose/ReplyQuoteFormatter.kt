package dev.octoshrimpy.quik.feature.compose

internal object ReplyQuoteFormatter {

    fun format(messageSummary: String, existingDraft: CharSequence): String {
        val summary = messageSummary.trimEnd()
        if (summary.isBlank()) return existingDraft.toString()

        val quote = summary.lineSequence().joinToString("\n") { line -> "> $line" }
        return if (existingDraft.isEmpty()) "$quote\n\n" else "$quote\n\n$existingDraft"
    }
}
