package com.miinfc.presentation.write

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miinfc.data.nfc.amiibo.RawExperimentalNtagWriter
import com.miinfc.domain.repository.AmiiboLibraryRepository
import com.miinfc.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface RawWriteState {
    data object WaitingForTag : RawWriteState
    data object Writing : RawWriteState
    data class Success(val pagesWritten: Int, val pagesVerified: Int) : RawWriteState
    data class Error(val message: String) : RawWriteState
}

@HiltViewModel
class RawWriteViewModel @Inject constructor(
    private val library: AmiiboLibraryRepository,
    private val rawStatus: RawWriteStatusRepository,
) : ViewModel() {
    private val writer = RawExperimentalNtagWriter()
    private val mutable = MutableStateFlow<RawWriteState>(RawWriteState.WaitingForTag)
    val state: StateFlow<RawWriteState> = mutable.asStateFlow()
    private var processing = false

    fun onTag(tag: Tag) {
        if (processing) return
        processing = true
        viewModelScope.launch {
            val source = library.selected.first() ?: run {
                mutable.value = RawWriteState.Error("Nenhum Amiibo selecionado."); processing = false; return@launch
            }
            mutable.value = RawWriteState.Writing
            writer.write(tag, source).onSuccess {
                rawStatus.set(RawWriteDiagnosticSnapshot(
                    status = RawPhysicalWriteStatus.COMPLETED,
                    targetUidRead = true, targetUidLengthValid = true,
                    ntag215Detected = true, tagWritable = true, pagesVerified = true,
                    keyUsedInPreparation = false, preparedForTargetUid = false,
                    pwdPackConfigApplied = false, locksApplied = false, switchCompatible = false,
                ))
                mutable.value = RawWriteState.Success(it.pagesWritten, it.pagesVerified)
            }.onFailure {
                rawStatus.set(RawWriteDiagnosticSnapshot(
                    status = RawPhysicalWriteStatus.FAILED,
                    targetUidRead = tag.id.isNotEmpty(), targetUidLengthValid = tag.id.size == 7,
                    keyUsedInPreparation = false, preparedForTargetUid = false,
                    pwdPackConfigApplied = false, locksApplied = false, switchCompatible = false,
                ))
                mutable.value = RawWriteState.Error(it.message ?: "Falha na gravação RAW.")
                processing = false
            }
        }
    }
}
