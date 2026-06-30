package com.miinfc.domain.usecase

import android.nfc.Tag
import com.miinfc.data.nfc.Ntag215Technology
import com.miinfc.data.nfc.Ntag215TechnologyFactory
import com.miinfc.data.nfc.Ntag215Validator
import com.miinfc.data.nfc.NfcDebugLogger
import com.miinfc.data.nfc.amiibo.AmiiboTagValidator
import com.miinfc.domain.model.NfcInspectionError
import com.miinfc.domain.model.NfcTagInfo
import com.miinfc.domain.model.Ntag215CompatibilityStatus
import com.miinfc.domain.model.Ntag215ValidationResult
import io.mockk.mockk
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateNtag215UseCaseTest {
    private val tag = mockk<Tag>(relaxed = true)

    @Test
    fun `confirms NTAG215 and reports empty writable capacity`() {
        val technology = FakeTechnology(version = ntag215Version())
        val useCase = useCaseWith(technology)

        val result = useCase(tag, basicInfo()) as Ntag215ValidationResult.Confirmed

        assertTrue(result.tagInfo.isNtag215)
        assertTrue(result.tagInfo.isWritable)
        assertTrue(result.tagInfo.isEmpty)
        assertFalse(result.tagInfo.isLocked)
        assertEquals(540, result.tagInfo.totalSizeBytes)
        assertEquals(135, result.tagInfo.pageCount)
        assertTrue(technology.closed)
    }

    @Test
    fun `rejects a different NTAG storage size`() {
        val technology = FakeTechnology(
            version = ntag215Version().also { it[6] = 0x0F },
        )

        val result = useCaseWith(technology)(tag, basicInfo())

        assertTrue(result is Ntag215ValidationResult.Incompatible)
        assertTrue(technology.closed)
    }

    @Test
    fun `returns incompatible when MifareUltralight is unavailable`() {
        val validator = Ntag215Validator(Ntag215TechnologyFactory { null }, silentLogger)
        val useCase = ValidateNtag215UseCase(AmiiboTagValidator(validator))

        val result = useCase(tag, basicInfo())

        assertTrue(result is Ntag215ValidationResult.Incompatible)
    }

    @Test
    fun `maps IO failure and always closes connection`() {
        val technology = FakeTechnology(
            version = ntag215Version(),
            connectError = IOException("read failed"),
        )

        val result = useCaseWith(technology)(tag, basicInfo())

        assertEquals(
            NfcInspectionError.READ_FAILED,
            (result as Ntag215ValidationResult.Error).reason,
        )
        assertTrue(technology.closed)
    }

    private fun useCaseWith(technology: Ntag215Technology): ValidateNtag215UseCase {
        val validator = Ntag215Validator(Ntag215TechnologyFactory { technology }, silentLogger)
        return ValidateNtag215UseCase(AmiiboTagValidator(validator))
    }

    private fun basicInfo() = NfcTagInfo(
        uid = "01020304",
        technologies = listOf("android.nfc.tech.MifareUltralight"),
        isNtag215 = false,
        isWritable = false,
        isEmpty = false,
        isLocked = false,
        totalSizeBytes = null,
        pageCount = null,
        compatibilityStatus = Ntag215CompatibilityStatus.UNKNOWN,
    )

    private fun ntag215Version() = byteArrayOf(
        0x00, 0x04, 0x04, 0x02, 0x01, 0x00, 0x11, 0x03,
    )

    private val silentLogger = NfcDebugLogger { }

    private class FakeTechnology(
        private val version: ByteArray,
        private val connectError: IOException? = null,
    ) : Ntag215Technology {
        var closed = false

        override fun connect() {
            connectError?.let { throw it }
        }

        override fun transceive(command: ByteArray): ByteArray = version
        override fun readPages(pageOffset: Int): ByteArray = ByteArray(16)
        override fun writePage(pageOffset: Int, data: ByteArray) = Unit
        override fun setTimeout(milliseconds: Int) = Unit
        override fun close() {
            closed = true
        }
    }
}
