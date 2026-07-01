package com.miinfc.domain.amiibo

import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RealAmiiboCryptoEngineTest {
    private val engine = RealAmiiboCryptoEngine()

    @Test fun `combined 160 byte retail candidate is structurally valid`() {
        val file = keyFile(ByteArray(80) { 1 } + ByteArray(80) { 2 })
        assertTrue(engine.validateKey(file))
        assertFalse(engine.cryptoPreparationAvailable)
    }

    @Test fun `invalid size and identical halves fail structural validation`() {
        assertFalse(engine.validateKey(keyFile(ByteArray(159))))
        assertFalse(engine.validateKey(keyFile(ByteArray(160) { 7 })))
    }

    @Test fun `valid key and 540 byte dump remain blocked without real preparation`() = runTest {
        val key = keyFile(ByteArray(80) { 1 } + ByteArray(80) { 2 })
        val source = AmiiboSourceFile("guardian", "Guardian.bin", AmiiboSourceFormat.RAW_BIN, ByteArray(540))
        assertEquals(AmiiboCryptoResult.CryptoNotImplemented, engine.prepareForTag(key, source, ByteArray(7)))
    }

    @Test fun `invalid UID is reported before preparation`() = runTest {
        val key = keyFile(ByteArray(80) { 1 } + ByteArray(80) { 2 })
        val source = AmiiboSourceFile("guardian", "Guardian.bin", AmiiboSourceFormat.RAW_BIN, ByteArray(540))
        assertEquals(AmiiboCryptoResult.InvalidUid, engine.prepareForTag(key, source, ByteArray(6)))
    }

    private fun keyFile(bytes: ByteArray): ImportedKeyFile {
        val file = File.createTempFile("retail-key", ".bin").apply { deleteOnExit(); writeBytes(bytes) }
        return ImportedKeyFile("key", "key_retail.bin", file.absolutePath, bytes.size.toLong(), KeyFileDetectedType.RAW_BINARY)
    }
}
