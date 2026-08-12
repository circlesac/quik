package dev.octoshrimpy.quik.feature.compose

import org.junit.Assert.assertEquals
import org.junit.Test

class ReplyQuoteFormatterTest {

    @Test
    fun `quotes every line and leaves space for a reply`() {
        assertEquals(
            "> first line\n> second line\n\n",
            ReplyQuoteFormatter.format("first line\nsecond line", "")
        )
    }

    @Test
    fun `preserves existing draft after quoted message`() {
        assertEquals(
            "> original\n\nAlready writing this",
            ReplyQuoteFormatter.format("original", "Already writing this")
        )
    }
}
