package com.miinfc.data.local

data class ParsedFlipperNfc(val pages: Map<Int, ByteArray>) {
    val pageCount: Int get() = pages.size
    val rawBytes: ByteArray get() = pages.toSortedMap().values.fold(ByteArray(0)) { a, b -> a + b }
}

class FlipperNfcParser {
    fun parse(bytes: ByteArray): ParsedFlipperNfc {
        val text = bytes.toString(Charsets.UTF_8)
        val declared = Regex("(?im)^Pages total:\\s*(\\d+)\\s*$").find(text)?.groupValues?.get(1)?.toInt()
            ?: throw IllegalArgumentException("Pages total ausente")
        val pages = Regex("(?im)^Page (\\d+):\\s*((?:[0-9a-f]{2}\\s*){4})$").findAll(text).associate { match ->
            match.groupValues[1].toInt() to match.groupValues[2].trim().split(Regex("\\s+")).map { it.toInt(16).toByte() }.toByteArray()
        }
        require(pages.size == declared && (0 until declared).all(pages::containsKey)) { "Páginas ausentes" }
        return ParsedFlipperNfc(pages)
    }
}
