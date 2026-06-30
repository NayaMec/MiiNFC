package com.miinfc.domain.usecase

import com.miinfc.domain.model.DumpSource
import com.miinfc.domain.model.ImportBinError
import com.miinfc.domain.model.ImportBinResult
import com.miinfc.domain.model.LocalBinFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportBinUseCaseTest {
    private val useCase = ImportBinUseCase(ValidateDumpUseCase())

    @Test
    fun `imports a local 540 byte bin in memory`() {
        val result = useCase(LocalBinFile("backup.bin", null, ByteArray(540)))

        val dump = (result as ImportBinResult.Success).dump
        assertEquals(540, dump.size)
        assertEquals(DumpSource.IMPORTED_FILE, dump.source)
        assertTrue(dump.isValid)
    }

    @Test
    fun `rejects empty file`() {
        val result = useCase(LocalBinFile("backup.bin", null, byteArrayOf()))

        assertEquals(ImportBinError.EMPTY_FILE, (result as ImportBinResult.Error).reason)
    }

    @Test
    fun `rejects incompatible dump size`() {
        val result = useCase(LocalBinFile("backup.bin", null, ByteArray(100)))

        assertEquals(ImportBinError.INVALID_SIZE, (result as ImportBinResult.Error).reason)
    }

    @Test
    fun `rejects non bin file with unrelated mime type`() {
        val result = useCase(LocalBinFile("backup.txt", "text/plain", ByteArray(540)))

        assertEquals(ImportBinError.INVALID_FILE_TYPE, (result as ImportBinResult.Error).reason)
    }
}
