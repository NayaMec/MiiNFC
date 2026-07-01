package com.miinfc.domain.amiibo

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ImportKeyUseCaseTest {
    @Test fun `Flipper NFC document is parsed and accepted as candidate`() = runTest {
        val state = ImportKeyUseCase(Keys(), SafeStubAmiiboCryptoEngine())(
            "key_retail.nfc", flipperKeyCandidate(),
        )
        assertTrue(state.hasImportedKeyFile)
        assertFalse(state.hasValidCryptoKey)
        assertTrue(state.readableFormat)
        assertTrue(state.acceptedAsKeyCandidate)
        assertEquals(KeyFileDetectedType.FLIPPER_NFC_DEVICE, state.detectedType)
        assertEquals("Arquivo lido com sucesso, mas o motor Amiibo atual não valida chaves.", state.message)
    }

    @Test fun `unavailable crypto cannot mark key valid`() = runTest {
        val state = ImportKeyUseCase(Keys(), SafeStubAmiiboCryptoEngine())("key.bin", ByteArray(160))
        assertTrue(state.hasImportedKeyFile)
        assertFalse(state.hasValidCryptoKey)
        assertTrue(state.formatAccepted)
    }

    @Test fun `key is valid only after engine validation`() = runTest {
        val state = ImportKeyUseCase(Keys(), FakeAmiiboCryptoEngine())("key.bin", ByteArray(160))
        assertTrue(state.hasValidCryptoKey)
    }

    private class Keys : AmiiboKeyStore {
        override suspend fun import(displayName: String, bytes: ByteArray) = Result.success(ImportedKeyFile("id", displayName, "/private", bytes.size.toLong()))
        override suspend fun read(file: ImportedKeyFile) = Result.success(ByteArray(160))
        override suspend fun activeKey(): ImportedKeyFile? = null
    }

    private fun flipperKeyCandidate() = buildString {
        appendLine("Filetype: Flipper NFC device")
        appendLine("Version: 2")
        appendLine("Device type: NTAG215")
        appendLine("Pages total: 135")
        repeat(135) { appendLine("Page $it: 00 00 00 00") }
    }.toByteArray()
}
