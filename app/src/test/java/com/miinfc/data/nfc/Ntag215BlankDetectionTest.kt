package com.miinfc.data.nfc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Ntag215BlankDetectionTest {
    private val validator = Ntag215Validator(Ntag215TechnologyFactory { null }, NfcDebugLogger { })

    @Test fun `ignores manufacturer and configuration pages`() {
        val pages = completeUserPages().toMutableMap().apply {
            this[0] = byteArrayOf(1, 2, 3, 4)
            this[3] = byteArrayOf(0xE1.toByte(), 0x10, 0x3E, 0)
            this[130] = byteArrayOf(1, 2, 3, 4)
        }
        assertTrue(validator.isBlankForAmiibo(pages))
    }

    @Test fun `accepts zero and factory FF bytes in user area`() {
        val pages = completeUserPages().toMutableMap().apply {
            this[40] = byteArrayOf(0, -1, 0, -1)
        }
        assertTrue(validator.isBlankForAmiibo(pages))
    }

    @Test fun `rejects significant user data`() {
        val pages = completeUserPages().toMutableMap().apply {
            this[12] = byteArrayOf(0, 0x42, 0, 0)
        }
        assertFalse(validator.isBlankForAmiibo(pages))
    }

    @Test fun `accepts an empty NDEF formatted NTAG215`() {
        val pages = completeUserPages().toMutableMap().apply {
            this[4] = byteArrayOf(0x03, 0x00, 0xFE.toByte(), 0x00)
        }
        assertTrue(validator.isBlankForAmiibo(pages))
    }

    @Test fun `accepts the standard empty NDEF record`() {
        val pages = completeUserPages().toMutableMap().apply {
            this[4] = byteArrayOf(0x03, 0x03, 0xD0.toByte(), 0x00)
            this[5] = byteArrayOf(0x00, 0xFE.toByte(), 0x00, 0x00)
        }
        assertTrue(validator.isBlankForAmiibo(pages))
    }

    @Test fun `rejects a non-empty NDEF message`() {
        val pages = completeUserPages().toMutableMap().apply {
            this[4] = byteArrayOf(0x03, 0x03, 0xD1.toByte(), 0x01)
            this[5] = byteArrayOf(0x00, 0xFE.toByte(), 0x00, 0x00)
        }
        assertFalse(validator.isBlankForAmiibo(pages))
    }

    @Test fun `rejects incomplete reads instead of guessing blank`() {
        assertFalse(validator.isBlankForAmiibo(mapOf(4 to ByteArray(4))))
    }

    private fun completeUserPages(): Map<Int, ByteArray> =
        (4..129).associateWith { ByteArray(4) }
}
