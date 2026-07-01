package com.miinfc.data.nfc

import android.content.Context
import android.nfc.Tag
import com.miinfc.domain.model.*
import java.io.IOException

fun interface NfcHardwareGateway { fun isEnabled(context: Context): Boolean? }
class NfcAvailabilityChecker(private val hardwareGateway: NfcHardwareGateway) {
    fun checkAvailability(context: Context) = when (hardwareGateway.isEnabled(context)) {
        true -> NfcAvailabilityStatus.AVAILABLE_ENABLED
        false -> NfcAvailabilityStatus.AVAILABLE_DISABLED
        null -> NfcAvailabilityStatus.NOT_SUPPORTED
    }
}
fun interface Ntag215TechnologyFactory { fun create(tag: Tag): Ntag215Technology? }
fun interface NfcDebugLogger { fun log(message: String) }
interface Ntag215Technology : AutoCloseable {
    fun connect(); fun transceive(command: ByteArray): ByteArray; fun readPages(pageOffset: Int): ByteArray
    fun writePage(pageOffset: Int, data: ByteArray); fun setTimeout(milliseconds: Int); override fun close()
}

class Ntag215Validator(private val factory: Ntag215TechnologyFactory, private val logger: NfcDebugLogger) {
    fun inspect(tag: Tag, base: NfcTagInfo): Ntag215ValidationResult {
        val tech = factory.create(tag) ?: return Ntag215ValidationResult.Incompatible(base)
        try {
            tech.connect(); tech.setTimeout(1000)
            val version = tech.transceive(byteArrayOf(0x60))
            if (version.size < 8 || version[6] != 0x11.toByte()) return Ntag215ValidationResult.Incompatible(base)
            val pages = mutableMapOf<Int, ByteArray>()
            for (page in 4..129 step 4) {
                val block = tech.readPages(page)
                if (block.size < 16) return Ntag215ValidationResult.Error(NfcInspectionError.READ_FAILED)
                repeat(4) { pages[page + it] = block.copyOfRange(it * 4, it * 4 + 4) }
            }
            return Ntag215ValidationResult.Confirmed(base.copy(
                isNtag215 = true, isWritable = true, isEmpty = isBlankForAmiibo(pages), isLocked = false,
                totalSizeBytes = 540, pageCount = 135, compatibilityStatus = Ntag215CompatibilityStatus.COMPATIBLE,
            ))
        } catch (e: IOException) {
            logger.log("Falha de I/O NFC: ${e.javaClass.simpleName}")
            return Ntag215ValidationResult.Error(NfcInspectionError.READ_FAILED)
        } finally { runCatching { tech.close() } }
    }

    fun isBlankForAmiibo(pages: Map<Int, ByteArray>): Boolean {
        if (!(4..129).all(pages::containsKey)) return false
        val bytes = (4..129).flatMap { pages.getValue(it).asIterable() }.toByteArray()
        if (bytes.all { it == 0.toByte() || it == 0xFF.toByte() }) return true
        val significant = bytes.dropWhile { it == 0.toByte() || it == 0xFF.toByte() }
        val emptyNdef = significant.take(3) == listOf(0x03.toByte(), 0x00, 0xFE.toByte()) ||
            significant.take(6) == listOf(0x03.toByte(), 0x03, 0xD0.toByte(), 0x00, 0x00, 0xFE.toByte())
        return emptyNdef && significant.drop(if (significant[1] == 0.toByte()) 3 else 6).all { it == 0.toByte() || it == 0xFF.toByte() }
    }
}
