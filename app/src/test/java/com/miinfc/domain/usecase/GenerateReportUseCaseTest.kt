package com.miinfc.domain.usecase

import com.miinfc.domain.model.OperationReport
import com.miinfc.domain.model.OperationType
import com.miinfc.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateReportUseCaseTest {
    @Test fun `creates and saves success report`() = runTest {
        val repository = FakeReportRepository()
        val report = GenerateReportUseCase(repository)(
            OperationType.TAG_WRITE, success = true, verified = true,
        )
        assertTrue(report.success)
        assertTrue(report.verified)
        assertNull(report.errorMessage)
        assertEquals(report, repository.saved)
    }

    @Test fun `creates and saves failure report`() = runTest {
        val repository = FakeReportRepository()
        val report = GenerateReportUseCase(repository)(
            OperationType.TAG_READ, success = false, errorMessage = "READ_FAILED",
        )
        assertFalse(report.success)
        assertEquals("READ_FAILED", report.errorMessage)
        assertEquals(report, repository.saved)
    }

    private class FakeReportRepository : ReportRepository {
        var saved: OperationReport? = null
        override suspend fun save(report: OperationReport) { saved = report }
        override fun getAll(): Flow<List<OperationReport>> = flowOf(listOfNotNull(saved))
        override suspend fun getById(id: String): OperationReport? = saved
    }
}
