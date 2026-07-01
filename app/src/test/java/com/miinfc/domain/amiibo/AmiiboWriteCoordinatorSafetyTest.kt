package com.miinfc.domain.amiibo

import com.miinfc.data.nfc.amiibo.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AmiiboWriteCoordinatorSafetyTest {
    @Test fun `missing crypto stops before physical write and locks`() = runTest {
        var writes = 0
        var finalizations = 0
        val keys = object : AmiiboKeyStore {
            override suspend fun import(displayName: String, bytes: ByteArray) = Result.failure<ImportedKeyFile>(UnsupportedOperationException())
            override suspend fun read(file: ImportedKeyFile) = Result.success(ByteArray(160))
            override suspend fun activeKey(): ImportedKeyFile? = null
        }
        val preparation = AmiiboPreparationUseCase(SafeStubAmiiboCryptoEngine())
        val coordinator = AmiiboWriteCoordinator(
            reader = object : AmiiboRawTagReader { override suspend fun read() = Result.success(AmiiboRawTag(ByteArray(7), emptyMap(), true, true, true)) },
            preparation = preparation,
            planBuilder = AmiiboWritePlanBuilder(),
            writer = object : AmiiboRawTagWriter { override suspend fun writeMainData(plan: AmiiboWritePlan): Result<Unit> { writes++; return Result.success(Unit) } },
            finalizer = object : AmiiboTagFinalizer { override suspend fun finalize(plan: AmiiboWritePlan): Result<Unit> { finalizations++; return Result.success(Unit) } },
            verifier = object : AmiiboTagVerifier {
                override suspend fun verifyMainData(plan: AmiiboWritePlan) = Result.success(true)
                override suspend fun verifyFinalized(plan: AmiiboWritePlan) = Result.success(AmiiboVerification(true, true, true))
            },
        )
        val outcome = coordinator.write(
            ImportedKeyFile("id", "key.bin", "/private"),
            AmiiboSourceFile("id", "amiibo.bin", AmiiboSourceFormat.RAW_BIN, ByteArray(540)),
        )
        assertTrue(outcome is AmiiboWriteOutcome.Failure)
        assertEquals(0, writes)
        assertEquals(0, finalizations)
    }
}
