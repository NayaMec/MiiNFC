package com.miinfc.domain.amiibo

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AmiiboPreparationUseCaseTest {
    private val key = ImportedKeyFile("key", "key_retail.bin", "/private/key", 160)
    private val source = AmiiboSourceFile("dump", "amiibo.bin", AmiiboSourceFormat.RAW_BIN, ByteArray(540))

    @Test fun `fails closed when crypto is not implemented`() = runTest {
        val useCase = AmiiboPreparationUseCase(SafeStubAmiiboCryptoEngine())
        val error = useCase.prepare(key, source, ByteArray(7)).exceptionOrNull()
        assertTrue(error is AmiiboPreparationException.MissingCryptoEngine)
    }

    @Test fun `rejects UID that is not seven bytes`() = runTest {
        val useCase = AmiiboPreparationUseCase(FakeAmiiboCryptoEngine())
        assertTrue(useCase.prepare(key, source, ByteArray(6)).exceptionOrNull() is AmiiboPreparationException.InvalidUid)
    }

    @Test fun `prepared image never writes physical UID pages`() = runTest {
        val useCase = AmiiboPreparationUseCase(FakeAmiiboCryptoEngine())
        val image = useCase.prepare(key, source, ByteArray(7)).getOrThrow()
        assertFalse(image.pagesToWrite.any { it in 0..2 })
        assertTrue(image.cryptographicallyPrepared)
        assertTrue(AmiiboWritePlanBuilder().build(image).steps.isNotEmpty())
    }

    @Test fun `plan verifies main data before irreversible locks`() {
        val image = AmiiboFinalizationPolicy().toPreparedImage(ByteArray(540))
        val steps = AmiiboWritePlanBuilder().build(image).steps
        val lastVerify = steps.indexOfLast { it is AmiiboWriteStep.VerifyPage }
        val firstLock = steps.indexOfFirst { it is AmiiboWriteStep.ApplyLock }
        assertTrue(firstLock > lastVerify)
    }

    @Test fun `maps invalid key result`() = runTest {
        val engine = FakeAmiiboCryptoEngine(AmiiboCryptoResult.InvalidKey)
        assertTrue(AmiiboPreparationUseCase(engine).prepare(key, source, ByteArray(7)).exceptionOrNull() is AmiiboPreparationException.MissingOrInvalidKey)
    }

    @Test fun `maps invalid UID result`() = runTest {
        val engine = FakeAmiiboCryptoEngine(AmiiboCryptoResult.InvalidUid)
        assertTrue(AmiiboPreparationUseCase(engine).prepare(key, source, ByteArray(7)).exceptionOrNull() is AmiiboPreparationException.InvalidUid)
    }
}
