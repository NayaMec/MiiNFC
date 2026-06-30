package com.miinfc.domain.usecase

import com.miinfc.domain.model.ImportBinError
import com.miinfc.domain.model.LocalBinFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateDumpUseCaseTest {
    private val useCase = ValidateDumpUseCase()

    @Test fun `empty file is rejected`() {
        assertEquals(ImportBinError.EMPTY_FILE, useCase(file(ByteArray(0))))
    }

    @Test fun `invalid size is rejected`() {
        assertEquals(ImportBinError.INVALID_SIZE, useCase(file(ByteArray(539))))
    }

    @Test fun `valid size is accepted`() {
        assertNull(useCase(file(ByteArray(540))))
    }

    private fun file(bytes: ByteArray) = LocalBinFile("backup.bin", null, bytes)
}
