package com.miinfc.domain.amiibo

/** Test-only engine. It exercises orchestration and never represents real Switch compatibility. */
class FakeAmiiboCryptoEngine(
    var resultOverride: AmiiboCryptoResult? = null,
) : AmiiboCryptoEngine {
    override val cryptoPreparationAvailable = true
    override fun validateKey(keyFile: ImportedKeyFile) = keyFile.byteSize > 0

    override suspend fun prepareForTag(keyFile: ImportedKeyFile, sourceFile: AmiiboSourceFile, targetUid: ByteArray): AmiiboCryptoResult {
        resultOverride?.let { return it }
        if (!validateKey(keyFile)) return AmiiboCryptoResult.InvalidKey
        if (targetUid.size != 7) return AmiiboCryptoResult.InvalidUid
        if (sourceFile.bytes.isEmpty()) return AmiiboCryptoResult.InvalidDump
        val pages = (0..134).associateWith { page -> byteArrayOf(page.toByte(), 0x11, 0x22, 0x33) }
        return AmiiboCryptoResult.Success(PreparedAmiiboImage(
            pages = pages, pagesToWrite = (3..129).toList(), pagesToVerify = (3..129).toList(),
            passwordPage = 133, packPage = 134, configPages = listOf(131, 132, 134),
            lockPages = listOf(130), cryptographicallyPrepared = true,
        ))
    }
}
