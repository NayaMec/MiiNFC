package com.miinfc.domain.amiibo

import com.miinfc.data.local.FlipperNfcParser

class AmiiboDumpNormalizer(private val flipperParser: FlipperNfcParser = FlipperNfcParser()) {
    fun normalize(source: AmiiboSourceFile): Result<ByteArray> = runCatching {
        val raw = when (source.format) {
            AmiiboSourceFormat.FLIPPER_NFC -> flipperParser.parse(source.bytes).rawBytes
            AmiiboSourceFormat.RAW_BIN -> source.bytes.copyOf()
        }
        when (raw.size) {
            540 -> raw
            // Known compact dumps omit immutable manufacturer bytes. They are placeholders only;
            // the crypto engine must rebuild them from the physical target UID.
            532 -> ByteArray(8) + raw
            520 -> ByteArray(20) + raw
            // Some tools append a 32-byte metadata trailer.
            572 -> raw.copyOfRange(0, 540)
            else -> throw AmiiboPreparationException.InvalidDump()
        }
    }
}
