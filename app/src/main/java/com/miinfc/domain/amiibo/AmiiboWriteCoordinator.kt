package com.miinfc.domain.amiibo

import com.miinfc.data.nfc.amiibo.*

sealed interface AmiiboWriteOutcome {
    data class Success(val verification: AmiiboVerification) : AmiiboWriteOutcome
    data class Failure(val stage: Stage, val cause: Throwable?) : AmiiboWriteOutcome
    enum class Stage { READ_TAG, INVALID_TAG, PREPARE, WRITE_MAIN, VERIFY_MAIN, FINALIZE, VERIFY_FINAL }
}

class AmiiboWriteCoordinator(
    private val reader: AmiiboRawTagReader,
    private val preparation: AmiiboPreparationUseCase,
    private val planBuilder: AmiiboWritePlanBuilder,
    private val writer: AmiiboRawTagWriter,
    private val finalizer: AmiiboTagFinalizer,
    private val verifier: AmiiboTagVerifier,
) {
    suspend fun write(key: ImportedKeyFile, source: AmiiboSourceFile): AmiiboWriteOutcome {
        val tag = reader.read().getOrElse { return AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.READ_TAG, it) }
        if (!tag.isNtag215 || !tag.isWritable || !tag.isBlank || tag.uid.size != 7)
            return AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.INVALID_TAG, null)
        val image = preparation.prepare(key, source, tag.uid).getOrElse {
            return AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.PREPARE, it)
        }
        val plan = runCatching { planBuilder.build(image) }.getOrElse {
            return AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.PREPARE, it)
        }
        writer.writeMainData(plan).getOrElse { return AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.WRITE_MAIN, it) }
        val mainOk = verifier.verifyMainData(plan).getOrElse { return AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.VERIFY_MAIN, it) }
        if (!mainOk) return AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.VERIFY_MAIN, null)
        finalizer.finalize(plan).getOrElse { return AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.FINALIZE, it) }
        val final = verifier.verifyFinalized(plan).getOrElse { return AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.VERIFY_FINAL, it) }
        return if (final.switchReady) AmiiboWriteOutcome.Success(final)
        else AmiiboWriteOutcome.Failure(AmiiboWriteOutcome.Stage.VERIFY_FINAL, null)
    }
}
