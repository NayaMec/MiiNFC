package com.miinfc.domain.model

enum class DumpSource { IMPORTED_FILE, SCANNED_TAG }
enum class ImportBinError { EMPTY_FILE, INVALID_SIZE, INVALID_FILE_TYPE, INVALID_STRUCTURE }
enum class AmiiboWriteError { INVALID_DUMP, NOT_NTAG215, TAG_NOT_EMPTY, TAG_LOCKED, PREPARATION_FAILED, CRYPTO_NOT_IMPLEMENTED, WRITE_FAILED, VERIFY_FAILED, FINALIZATION_FAILED, UNKNOWN }
enum class OperationType { TAG_READ, TAG_WRITE, FILE_IMPORT, KEY_IMPORT }
enum class NfcAvailabilityStatus { AVAILABLE_ENABLED, AVAILABLE_DISABLED, NOT_SUPPORTED }
enum class Ntag215CompatibilityStatus { UNKNOWN, COMPATIBLE, INCOMPATIBLE }
enum class NfcInspectionError { READ_FAILED, CONNECTION_FAILED }

data class LocalBinFile(val name: String, val mimeType: String?, val bytes: ByteArray)
data class AmiiboDump(
    val id: String, val fileName: String, val bytes: ByteArray, val size: Int,
    val characterName: String?, val gameSeries: String?, val amiiboSeries: String?,
    val amiiboType: String?, val figureId: String?, val importedAt: Long?,
    val isValid: Boolean, val source: DumpSource,
)

sealed interface ImportBinResult {
    data class Success(val dump: AmiiboDump) : ImportBinResult
    data class Error(val reason: ImportBinError) : ImportBinResult
}

data class WriteResult(
    val success: Boolean, val verified: Boolean, val pagesWritten: Int,
    val error: AmiiboWriteError?,
    val structureVerified: Boolean = false,
    val finalized: Boolean = false,
) {
    val switchCompatible: Boolean get() = success && verified && structureVerified && finalized
}

data class OperationReport(
    val id: String, val operationType: OperationType, val dumpId: String? = null,
    val fileName: String? = null, val tagUid: String? = null, val success: Boolean,
    val verified: Boolean, val locked: Boolean = false, val errorMessage: String? = null,
    val timestamp: Long,
)

data class NfcTagInfo(
    val uid: String, val technologies: List<String>, val isNtag215: Boolean,
    val isWritable: Boolean, val isEmpty: Boolean, val isLocked: Boolean,
    val totalSizeBytes: Int?, val pageCount: Int?,
    val compatibilityStatus: Ntag215CompatibilityStatus,
)

sealed interface Ntag215ValidationResult {
    data class Confirmed(val tagInfo: NfcTagInfo) : Ntag215ValidationResult
    data class Incompatible(val tagInfo: NfcTagInfo) : Ntag215ValidationResult
    data class Error(val reason: NfcInspectionError) : Ntag215ValidationResult
}
