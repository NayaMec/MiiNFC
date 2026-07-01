package com.miinfc.presentation.scan

import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miinfc.domain.amiibo.*
import com.miinfc.domain.repository.AmiiboLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ScanState { data object Waiting : ScanState; data object Reading : ScanState; data class Success(val name: String) : ScanState; data class Error(val message: String) : ScanState }

@HiltViewModel
class ScanAmiiboViewModel @Inject constructor(private val library: AmiiboLibraryRepository) : ViewModel() {
    private val mutable = MutableStateFlow<ScanState>(ScanState.Waiting)
    val state: StateFlow<ScanState> = mutable.asStateFlow()
    fun scan(tag: Tag) = viewModelScope.launch(Dispatchers.IO) {
        mutable.value = ScanState.Reading
        runCatching {
            require(tag.id.size == 7) { "UID NFC inválido." }
            val tech = MifareUltralight.get(tag) ?: error("A tag não suporta Mifare Ultralight.")
            tech.use {
                it.connect()
                val version = it.transceive(byteArrayOf(0x60))
                require(version.size >= 8 && version[6] == 0x11.toByte()) { "A tag detectada não é NTAG215." }
                val all = ArrayList<Byte>(544)
                for (page in 0..132 step 4) all += it.readPages(page).toList()
                val bytes = all.take(540).toByteArray()
                val name = "Amiibo-${tag.id.joinToString("") { b -> "%02X".format(b) }}.bin"
                val source = AmiiboSourceFile(UUID.randomUUID().toString(), name, AmiiboSourceFormat.RAW_BIN, bytes)
                library.save(source); library.selectAmiibo(source.id)
                name
            }
        }.onSuccess { mutable.value = ScanState.Success(it) }
            .onFailure { mutable.value = ScanState.Error(it.message ?: "Falha ao ler a tag.") }
    }
}
