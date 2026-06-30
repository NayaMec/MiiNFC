package com.miinfc.data.local

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FlipperNfcParserTest {
    private val parser = FlipperNfcParser()

    @Test fun `parses every declared page without using text file size`() {
        val document = buildString {
            appendLine("Filetype: Flipper NFC device")
            appendLine("Version: 2")
            appendLine("Pages total: 3")
            appendLine("Page 0: 00 01 02 03")
            appendLine("Page 1: 04 05 06 07")
            appendLine("Page 2: 08 09 0A 0B")
        }

        val parsed = parser.parse(document.toByteArray())

        assertEquals(3, parsed.pageCount)
        assertArrayEquals(ByteArray(12) { it.toByte() }, parsed.rawBytes)
    }

    @Test fun `supports complete NTAG215 files with 135 pages`() {
        val document = buildString {
            appendLine("Filetype: Flipper NFC device")
            appendLine("Pages total: 135")
            repeat(135) { appendLine("Page $it: 00 00 00 00") }
        }

        val parsed = parser.parse(document.toByteArray())

        assertEquals(135, parsed.pageCount)
        assertEquals(540, parsed.rawBytes.size)
    }

    @Test fun `rejects missing pages instead of returning truncated data`() {
        val document = """
            Filetype: Flipper NFC device
            Pages total: 2
            Page 0: 00 00 00 00
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(document.toByteArray())
        }
    }
}
