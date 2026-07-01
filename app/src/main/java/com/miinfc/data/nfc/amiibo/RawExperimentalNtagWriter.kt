package com.miinfc.data.nfc.amiibo

import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import com.miinfc.domain.amiibo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RawWriteReceipt(val pagesWritten: Int, val pagesVerified: Int)

class RawExperimentalNtagWriter(
    private val normalizer: AmiiboDumpNormalizer = AmiiboDumpNormalizer(),
) {
    suspend fun write(tag: Tag, source: AmiiboSourceFile): Result<RawWriteReceipt> = withContext(Dispatchers.IO) {
        runCatching {
            require(tag.id.size == 7) { "UID da tag deve possuir 7 bytes." }
            val image = normalizer.normalize(source).getOrThrow()
            require(image.size == 540) { "Imagem RAW NTAG215 inválida." }
            val tech = MifareUltralight.get(tag) ?: error("Tecnologia Mifare Ultralight indisponível.")
            tech.use {
                it.connect()
                it.timeout = 2_000
                val version = it.transceive(byteArrayOf(0x60))
                require(version.size >= 8 && version[6] == 0x11.toByte()) { "A tag detectada não é NTAG215." }
                val manufacturer = it.readPages(0)
                val tail = it.readPages(128)
                require(manufacturer.size >= 12 && tail.size >= 12) { "Não foi possível inspecionar a tag." }
                val staticLocksClear = manufacturer[10] == 0.toByte() && manufacturer[11] == 0.toByte()
                val dynamicLocksClear = tail[8] == 0.toByte() && tail[9] == 0.toByte() && tail[10] == 0.toByte()
                require(staticLocksClear && dynamicLocksClear) { "A tag está bloqueada ou parcialmente bloqueada." }

                // User data only. Pages 0..3 and 130..134 are deliberately untouched.
                for (page in RawExperimentalWritePolicy.pagesToWrite) it.writePage(page, image.copyOfRange(page * 4, page * 4 + 4))

                var verified = 0
                for (start in 4..128 step 4) {
                    val actual = it.readPages(start)
                    val lastPage = minOf(start + 3, 129)
                    val expected = image.copyOfRange(start * 4, (lastPage + 1) * 4)
                    require(actual.size >= expected.size && actual.copyOfRange(0, expected.size).contentEquals(expected)) {
                        "Falha de verificação na página $start."
                    }
                    verified += lastPage - start + 1
                }
                RawWriteReceipt(pagesWritten = 126, pagesVerified = verified)
            }
        }
    }
}
