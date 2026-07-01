package com.miinfc.presentation.importfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miinfc.data.local.FlipperNfcParser
import com.miinfc.domain.amiibo.*
import com.miinfc.domain.repository.AmiiboLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ImportAmiiboFileViewModel @Inject constructor(
    private val library: AmiiboLibraryRepository,
) : ViewModel() {
    private val normalizer = AmiiboDumpNormalizer()
    private val flipper = FlipperNfcParser()
    private val mutable = MutableStateFlow<AmiiboFileImportState>(AmiiboFileImportState.Idle)
    val state: StateFlow<AmiiboFileImportState> = mutable.asStateFlow()

    fun import(type: AmiiboImportFileType, name: String, bytes: ByteArray) = viewModelScope.launch {
        mutable.value = AmiiboFileImportState.Loading
        runCatching {
            val textPrefix = bytes.take(128).toByteArray().toString(Charsets.UTF_8)
            val isFlipper = textPrefix.contains("Filetype: Flipper NFC device", true)
            require(name.endsWith(".bin", true) || name.endsWith(".nfc", true)) { "Selecione um arquivo .bin ou .nfc." }
            val format = if (isFlipper) AmiiboSourceFormat.FLIPPER_NFC else AmiiboSourceFormat.RAW_BIN
            val source = AmiiboSourceFile(UUID.randomUUID().toString(), name, format, bytes.copyOf())
            val detail = if (!isFlipper) {
                require(name.endsWith(".bin", true)) { "Arquivos binários brutos devem usar a extensão .bin." }
                normalizer.normalize(source).getOrThrow()
                "Arquivo BIN válido · ${bytes.size} bytes"
            } else {
                val text = bytes.toString(Charsets.UTF_8)
                require(text.contains("Filetype: Flipper NFC device", true)) { "O arquivo não está no formato Flipper NFC device." }
                require(Regex("(?im)^Device type:\\s*NTAG215\\s*$").containsMatchIn(text)) { "O arquivo NFC não declara Device type: NTAG215." }
                val parsed = flipper.parse(bytes)
                require(parsed.pageCount == 135) { "NTAG215 deve possuir 135 páginas." }
                normalizer.normalize(source).getOrThrow()
                "Flipper NFC device · NTAG215 · ${parsed.pageCount} páginas"
            }
            library.save(source)
            library.selectAmiibo(source.id)
            AmiiboFileImportState.Success(source, detail)
        }.onSuccess { mutable.value = it }
            .onFailure { mutable.value = AmiiboFileImportState.Error(it.message ?: "Não foi possível importar o arquivo.") }
    }
}
