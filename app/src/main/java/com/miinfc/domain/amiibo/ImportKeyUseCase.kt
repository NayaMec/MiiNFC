package com.miinfc.domain.amiibo

import com.miinfc.data.local.FlipperNfcParser

data class ImportKeyUiState(
    val fileSelected: Boolean = false,
    val fileName: String? = null,
    val detectedType: KeyFileDetectedType = KeyFileDetectedType.UNKNOWN,
    val readableFormat: Boolean = false,
    val acceptedAsKeyCandidate: Boolean = false,
    val keyStructurallyValid: Boolean = false,
    val cryptoEngineAvailable: Boolean = false,
    val importedFile: ImportedKeyFile? = null,
    val message: String? = null,
) {
    val hasImportedKeyFile get() = fileSelected
    val hasValidCryptoKey get() = keyStructurallyValid
    val validatedByEngine get() = keyStructurallyValid
    val formatAccepted get() = readableFormat
    val errorMessage get() = message
}
typealias ImportKeyState = ImportKeyUiState

class ImportKeyUseCase(
    private val keyStore: AmiiboKeyStore,
    private val crypto: AmiiboCryptoEngine,
    private val flipperParser: FlipperNfcParser = FlipperNfcParser(),
) {
    suspend operator fun invoke(name: String, bytes: ByteArray): ImportKeyUiState {
        val engineAvailable = crypto.cryptoPreparationAvailable
        val header = bytes.take(160).toByteArray().toString(Charsets.UTF_8)
        val isFlipper = header.contains("Filetype: Flipper NFC device", ignoreCase = true)
        val candidate = if (isFlipper) parseFlipperCandidate(name, bytes, engineAvailable) else parseTraditionalCandidate(name, bytes, engineAvailable)
        if (!candidate.readableFormat || !candidate.acceptedAsKeyCandidate) return candidate

        val candidateBytes = if (isFlipper) flipperParser.parse(bytes).rawBytes else bytes
        val stored = keyStore.import(name, candidateBytes).getOrElse {
            return candidate.copy(message = "Arquivo lido, mas não foi possível salvá-lo no armazenamento privado.")
        }.copy(detectedType = candidate.detectedType, pageCount = if (isFlipper) 135 else null)
        val valid = crypto.validateKey(stored)
        return candidate.copy(
            keyStructurallyValid = valid,
            importedFile = stored,
            message = if (valid) "Formato da chave válido. Motor Amiibo ainda não integrado."
            else "Arquivo lido com sucesso, mas o motor Amiibo atual não valida chaves.",
        )
    }

    private fun parseFlipperCandidate(name: String, bytes: ByteArray, engineAvailable: Boolean): ImportKeyUiState = runCatching {
        val text = bytes.toString(Charsets.UTF_8)
        require(Regex("(?im)^Device type:\\s*NTAG215\\s*$").containsMatchIn(text)) { "O arquivo Flipper não declara uma NTAG215." }
        val parsed = flipperParser.parse(bytes)
        require(parsed.pageCount == 135) { "O arquivo Flipper deve possuir 135 páginas." }
        ImportKeyUiState(true, name, KeyFileDetectedType.FLIPPER_NFC_DEVICE,
            readableFormat = true, acceptedAsKeyCandidate = true, cryptoEngineAvailable = engineAvailable)
    }.getOrElse {
        ImportKeyUiState(true, name, KeyFileDetectedType.UNSUPPORTED,
            cryptoEngineAvailable = engineAvailable, message = it.message ?: "Não foi possível ler o arquivo Flipper.")
    }

    private fun parseTraditionalCandidate(name: String, bytes: ByteArray, engineAvailable: Boolean): ImportKeyUiState {
        val detected = when {
            bytes.size in setOf(80, 160) -> KeyFileDetectedType.RAW_BINARY
            name.endsWith(".txt", true) || name.endsWith(".keys", true) -> KeyFileDetectedType.TEXT_KEY
            else -> KeyFileDetectedType.UNSUPPORTED
        }
        val readable = detected != KeyFileDetectedType.UNSUPPORTED
        return ImportKeyUiState(true, name, detected, readable, readable,
            cryptoEngineAvailable = engineAvailable,
            message = if (readable) null else "Formato de arquivo de chave não reconhecido.")
    }
}
