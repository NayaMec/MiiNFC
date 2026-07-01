package com.miinfc.domain.amiibo

class AmiiboPreparationUseCase(
    private val crypto: AmiiboCryptoEngine,
    private val logger: AmiiboSafeLogger = AmiiboSafeLogger { },
) {
    suspend fun prepare(keyFile: ImportedKeyFile, sourceFile: AmiiboSourceFile, targetTagUid: ByteArray): Result<PreparedAmiiboImage> {
        logger.log("key file imported: true")
        val engineRegistered = crypto !is SafeStubAmiiboCryptoEngine
        logger.log("crypto engine available: ${crypto.cryptoPreparationAvailable}")
        logger.log("target UID length: ${if (targetTagUid.size == 7) "7" else "invalid"}")
        if (targetTagUid.size != 7) return Result.failure(AmiiboPreparationException.InvalidUid())
        val keyValid = crypto.validateKey(keyFile)
        logger.log("key valid: $keyValid")
        if (!keyValid && engineRegistered) return Result.failure(AmiiboPreparationException.MissingOrInvalidKey())
        return when (val result = crypto.prepareForTag(keyFile, sourceFile, targetTagUid)) {
            is AmiiboCryptoResult.Success -> {
                logger.log("prepared image generated: true")
                if (result.preparedImage.cryptographicallyPrepared) Result.success(result.preparedImage)
                else Result.failure(AmiiboPreparationException.PreparationFailed())
            }
            AmiiboCryptoResult.MissingKey, AmiiboCryptoResult.InvalidKey -> Result.failure(AmiiboPreparationException.MissingOrInvalidKey())
            AmiiboCryptoResult.InvalidDump -> Result.failure(AmiiboPreparationException.InvalidDump())
            AmiiboCryptoResult.InvalidUid -> Result.failure(AmiiboPreparationException.InvalidUid())
            AmiiboCryptoResult.CryptoNotImplemented -> Result.failure(AmiiboPreparationException.MissingCryptoEngine())
            is AmiiboCryptoResult.Error -> Result.failure(AmiiboPreparationException.PreparationFailed(IllegalStateException(result.message)))
        }
    }
}

class AmiiboFinalizationPolicy {
    fun toPreparedImage(image: ByteArray): PreparedAmiiboImage {
        require(image.size == 540)
        val pages = (0..134).associateWith { image.copyOfRange(it * 4, it * 4 + 4) }
        // Pages 0..2 contain immutable UID/manufacturer data and are never copied from a dump.
        val main = (3..129).toList()
        return PreparedAmiiboImage(
            pages = pages, pagesToWrite = main, pagesToVerify = main,
            passwordPage = 133, packPage = 134, configPages = listOf(131, 132, 134),
            lockPages = listOf(130), cryptographicallyPrepared = true,
        )
    }
}

class AmiiboWritePlanBuilder {
    fun build(image: PreparedAmiiboImage): AmiiboWritePlan {
        require(image.cryptographicallyPrepared) { "Imagem não foi preparada criptograficamente" }
        val mainWrite = image.pagesToWrite.map { AmiiboWriteStep.WritePage(it, image.page(it)) }
        val mainVerify = image.pagesToVerify.map { AmiiboWriteStep.VerifyPage(it, image.page(it)) }
        // Irreversible lock operations are deliberately last, after main-data verification.
        val password = image.passwordPage?.let { listOf(AmiiboWriteStep.ApplyPassword(it, image.page(it))) }.orEmpty()
        val config = image.configPages.distinct().filterNot { it == image.passwordPage }.map { AmiiboWriteStep.ApplyConfig(it, image.page(it)) }
        val locks = image.lockPages.map { AmiiboWriteStep.ApplyLock(it, image.page(it)) }
        return AmiiboWritePlan(image, mainWrite + mainVerify + password + config + locks)
    }

    private fun PreparedAmiiboImage.page(page: Int) = requireNotNull(pages[page]) { "Página $page ausente" }.also { require(it.size == 4) }
}
