package com.miinfc.domain.usecase

import android.nfc.Tag
import com.miinfc.domain.model.AmiiboDump
import com.miinfc.domain.model.AmiiboWriteError
import com.miinfc.domain.model.DumpSource
import com.miinfc.domain.model.OperationReport
import com.miinfc.domain.model.WriteResult
import com.miinfc.domain.repository.Ntag215Writer
import com.miinfc.domain.repository.ReportRepository
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WriteAmiiboUseCaseTest {
    private val tag = mockk<Tag>(relaxed = true)

    @Test fun `rejects invalid dump before writer`() = runTest {
        val result = useCase(result(AmiiboWriteError.UNKNOWN))(invalidDump())
        assertEquals(AmiiboWriteError.INVALID_DUMP, result.error)
    }

    @Test fun `returns not NTAG215`() = runTest {
        val result = useCase(result(AmiiboWriteError.NOT_NTAG215))(validDump())
        assertEquals(AmiiboWriteError.NOT_NTAG215, result.error)
    }

    @Test fun `returns already written tag`() = runTest {
        val result = useCase(result(AmiiboWriteError.TAG_NOT_EMPTY))(validDump())
        assertEquals(AmiiboWriteError.TAG_NOT_EMPTY, result.error)
    }

    @Test fun `succeeds only with approved verification`() = runTest {
        val result = useCase(WriteResult(true, true, 126, null))(validDump())
        assertTrue(result.success)
        assertTrue(result.verified)
    }

    @Test fun `fails when verification is rejected`() = runTest {
        val result = useCase(result(AmiiboWriteError.VERIFY_FAILED))(validDump())
        assertFalse(result.success)
        assertFalse(result.verified)
    }

    private fun useCase(writerResult: WriteResult): suspend (AmiiboDump) -> WriteResult {
        val writer = Ntag215Writer<Tag> { _, _, _ -> writerResult }
        val generate = GenerateReportUseCase(FakeReportRepository())
        val useCase = WriteAmiiboUseCase(writer, generate)
        return { dump -> useCase(tag, dump) }
    }

    private fun result(error: AmiiboWriteError) = WriteResult(false, false, 0, error)
    private fun validDump() = dump(true, ByteArray(540))
    private fun invalidDump() = dump(false, ByteArray(540))
    private fun dump(valid: Boolean, bytes: ByteArray) = AmiiboDump(
        "id", "backup.bin", bytes, bytes.size, null, null, null, null, null, null,
        valid, DumpSource.IMPORTED_FILE,
    )

    private class FakeReportRepository : ReportRepository {
        override suspend fun save(report: OperationReport) = Unit
        override fun getAll(): Flow<List<OperationReport>> = emptyFlow()
        override suspend fun getById(id: String): OperationReport? = null
    }
}
