package com.miinfc.presentation.importkey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miinfc.domain.amiibo.*
import com.miinfc.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ImportKeyViewModel @Inject constructor(
    keyStore: AmiiboKeyStore,
    engineProvider: AmiiboCryptoEngineProvider,
    private val keyStatus: KeyStatusRepository,
) : ViewModel() {
    private val useCase = ImportKeyUseCase(keyStore, engineProvider.get())
    private val mutable = MutableStateFlow(ImportKeyState())
    val state: StateFlow<ImportKeyState> = mutable.asStateFlow()

    fun import(name: String, bytes: ByteArray) = viewModelScope.launch {
        val result = useCase(name, bytes)
        mutable.value = result
        keyStatus.update(KeyStatus(
            imported = result.hasImportedKeyFile,
            candidateAccepted = result.acceptedAsKeyCandidate,
            valid = result.hasValidCryptoKey,
            file = result.importedFile,
        ))
    }
}
