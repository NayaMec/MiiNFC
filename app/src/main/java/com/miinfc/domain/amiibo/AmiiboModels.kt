package com.miinfc.domain.amiibo

enum class KeyFileDetectedType { UNKNOWN, RAW_BINARY, TEXT_KEY, FLIPPER_NFC_DEVICE, UNSUPPORTED }
enum class WriteMode { SWITCH_COMPATIBLE, RAW_EXPERIMENTAL }
data class ImportedKeyFile(
    val id: String,
    val displayName: String,
    val privatePath: String,
    val byteSize: Long = 0,
    val detectedType: KeyFileDetectedType = KeyFileDetectedType.UNKNOWN,
    val pageCount: Int? = null,
)
enum class AmiiboCompatibilityStatus {
    NOT_READY, MISSING_KEY, INVALID_KEY, MISSING_AMIIBO_FILE, MISSING_CRYPTO_ENGINE,
    READY_TO_PREPARE, PREPARED_FOR_TARGET_UID, PHYSICALLY_WRITTEN, FINALIZED,
    VERIFIED_SWITCH_COMPATIBLE,
}
enum class AmiiboSourceFormat { RAW_BIN, FLIPPER_NFC }
data class AmiiboSourceFile(
    val id: String,
    val displayName: String,
    val format: AmiiboSourceFormat,
    val bytes: ByteArray,
)
data class AmiiboWriteContext(
    val keyFile: ImportedKeyFile,
    val sourceFile: AmiiboSourceFile,
    val targetTagUid: ByteArray,
)

data class PreparedAmiiboImage(
    val pages: Map<Int, ByteArray>,
    val pagesToWrite: List<Int>,
    val pagesToVerify: List<Int>,
    val passwordPage: Int?,
    val packPage: Int?,
    val configPages: List<Int>,
    val lockPages: List<Int>,
    val cryptographicallyPrepared: Boolean,
)

data class AmiiboWritePlan(val preparedImage: PreparedAmiiboImage, val steps: List<AmiiboWriteStep>)
sealed interface AmiiboWriteStep {
    data class WritePage(val page: Int, val data: ByteArray) : AmiiboWriteStep
    data class VerifyPage(val page: Int, val expected: ByteArray) : AmiiboWriteStep
    data class ApplyPassword(val page: Int, val data: ByteArray) : AmiiboWriteStep
    data class ApplyConfig(val page: Int, val data: ByteArray) : AmiiboWriteStep
    data class ApplyLock(val page: Int, val data: ByteArray) : AmiiboWriteStep
}

sealed class AmiiboPreparationException(message: String) : Exception(message) {
    class MissingOrInvalidKey : AmiiboPreparationException("Arquivo de chave ausente ou inválido")
    class InvalidKey : AmiiboPreparationException("Arquivo de chave ausente ou inválido")
    class InvalidDump : AmiiboPreparationException("Arquivo Amiibo inválido")
    class InvalidUid : AmiiboPreparationException("UID alvo deve possuir 7 bytes")
    class CryptoNotImplemented : AmiiboPreparationException("Motor criptográfico Amiibo não implementado")
    class MissingCryptoEngine : AmiiboPreparationException("Motor criptográfico Amiibo não implementado")
    class PreparationFailed(cause: Throwable? = null) : AmiiboPreparationException("Não foi possível preparar os dados para esta tag") { init { cause?.let(::initCause) } }
}

data class AmiiboDiagnosticReport(
    val fileName: String?, val fileType: String?, val fileSize: Int? = null,
    val fileNormalized: Boolean, val keyImported: Boolean, val keyValidated: Boolean,
    val keyFormatAccepted: Boolean = false, val cryptoEngineAvailable: Boolean,
    val engineRegistered: Boolean = true, val engineFunctional: Boolean = cryptoEngineAvailable,
    val targetUidRead: Boolean, val targetUidLengthValid: Boolean, val isNtag215: Boolean,
    val tagBlankBeforeWrite: Boolean = false, val preparedForTargetUid: Boolean,
    val switchImageGenerated: Boolean = false, val physicalWriteOk: Boolean,
    val pagesVerified: Boolean = false, val configApplied: Boolean = false,
    val locksApplied: Boolean = false, val finalizationApplied: Boolean,
    val verificationOk: Boolean, val switchCompatible: Boolean, val failureReason: String?,
    val nfcAvailable: Boolean = false, val nfcEnabled: Boolean = false,
    val isPhysicalDevice: Boolean = true, val tagWritable: Boolean = false,
    val rawPhysicalWriteStatus: String = "Não realizada",
    val keyUsedInPreparation: Boolean = false,
    val pwdPackConfigApplied: Boolean = false,
)
