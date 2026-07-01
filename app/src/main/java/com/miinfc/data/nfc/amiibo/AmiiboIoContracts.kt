package com.miinfc.data.nfc.amiibo

import com.miinfc.domain.amiibo.AmiiboWritePlan

data class AmiiboRawTag(val uid: ByteArray, val pages: Map<Int, ByteArray>, val isNtag215: Boolean, val isWritable: Boolean, val isBlank: Boolean)
interface AmiiboRawTagReader { suspend fun read(): Result<AmiiboRawTag> }
interface AmiiboRawTagWriter { suspend fun writeMainData(plan: AmiiboWritePlan): Result<Unit> }
interface AmiiboTagFinalizer { suspend fun finalize(plan: AmiiboWritePlan): Result<Unit> }
data class AmiiboVerification(val physicalWriteOk: Boolean, val structureOk: Boolean, val finalizationOk: Boolean) {
    val switchReady: Boolean get() = physicalWriteOk && structureOk && finalizationOk
}
interface AmiiboTagVerifier {
    suspend fun verifyMainData(plan: AmiiboWritePlan): Result<Boolean>
    suspend fun verifyFinalized(plan: AmiiboWritePlan): Result<AmiiboVerification>
}
