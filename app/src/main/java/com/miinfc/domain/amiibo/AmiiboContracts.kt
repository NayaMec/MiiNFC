package com.miinfc.domain.amiibo

interface AmiiboKeyStore {
    suspend fun import(displayName: String, bytes: ByteArray): Result<ImportedKeyFile>
    suspend fun read(file: ImportedKeyFile): Result<ByteArray>
    suspend fun activeKey(): ImportedKeyFile?
}

interface AmiiboCryptoEngine {
    val cryptoPreparationAvailable: Boolean
    fun validateKey(keyFile: ImportedKeyFile): Boolean
    suspend fun prepareForTag(
        keyFile: ImportedKeyFile,
        sourceFile: AmiiboSourceFile,
        targetUid: ByteArray,
    ): AmiiboCryptoResult
}

sealed interface AmiiboCryptoResult {
    data class Success(val preparedImage: PreparedAmiiboImage) : AmiiboCryptoResult
    data object MissingKey : AmiiboCryptoResult
    data object InvalidKey : AmiiboCryptoResult
    data object InvalidDump : AmiiboCryptoResult
    data object InvalidUid : AmiiboCryptoResult
    data object CryptoNotImplemented : AmiiboCryptoResult
    data class Error(val message: String) : AmiiboCryptoResult
}

class SafeStubAmiiboCryptoEngine : AmiiboCryptoEngine {
    override val cryptoPreparationAvailable = false
    override fun validateKey(keyFile: ImportedKeyFile) = false
    override suspend fun prepareForTag(keyFile: ImportedKeyFile, sourceFile: AmiiboSourceFile, targetUid: ByteArray) =
        AmiiboCryptoResult.CryptoNotImplemented
}

/**
 * Production integration boundary. It validates the user-provided combined retail-key
 * container without exposing its contents. Cryptographic preparation remains fail-closed
 * until a reviewed backend is supplied.
 */
class RealAmiiboCryptoEngine : AmiiboCryptoEngine {
    override val cryptoPreparationAvailable = false

    override fun validateKey(keyFile: ImportedKeyFile): Boolean = runCatching {
        val file = java.io.File(keyFile.privatePath)
        if (!file.isFile || file.length() != 160L) return false
        val bytes = file.readBytes()
        if (bytes.size != 160) return false
        val first = bytes.copyOfRange(0, 80)
        val second = bytes.copyOfRange(80, 160)
        first.isNotEmpty() && second.isNotEmpty() && !first.contentEquals(second)
    }.getOrDefault(false)

    override suspend fun prepareForTag(
        keyFile: ImportedKeyFile,
        sourceFile: AmiiboSourceFile,
        targetUid: ByteArray,
    ): AmiiboCryptoResult {
        if (!validateKey(keyFile)) return AmiiboCryptoResult.InvalidKey
        if (targetUid.size != 7) return AmiiboCryptoResult.InvalidUid
        if (AmiiboDumpNormalizer().normalize(sourceFile).isFailure) return AmiiboCryptoResult.InvalidDump
        return AmiiboCryptoResult.CryptoNotImplemented
    }
}

fun interface AmiiboSafeLogger { fun log(message: String) }

fun interface AmiiboCryptoEngineProvider { fun get(): AmiiboCryptoEngine }
