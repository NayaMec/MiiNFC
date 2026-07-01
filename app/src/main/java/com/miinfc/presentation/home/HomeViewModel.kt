package com.miinfc.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miinfc.domain.amiibo.*
import com.miinfc.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    engineProvider: AmiiboCryptoEngineProvider,
    private val keyStore: AmiiboKeyStore,
    private val keyStatus: KeyStatusRepository,
    library: AmiiboLibraryRepository,
) : ViewModel() {
    private val engine = engineProvider.get()
    private val engineAvailable = engine.cryptoPreparationAvailable

    val state: StateFlow<HomeUiState> = combine(keyStatus.status, library.selected) { key, selected ->
        HomeUiState(
            hasImportedKeyFile = key.imported,
            hasAcceptedKeyCandidate = key.candidateAccepted,
            hasValidKey = key.valid,
            hasSelectedAmiibo = selected != null,
            selectedAmiiboName = selected?.displayName,
            selectedAmiiboSeries = selected?.let { if (it.format == AmiiboSourceFormat.RAW_BIN) "Arquivo BIN · válido" else "Arquivo NFC compatível · válido" },
            cryptoEngineAvailable = engineAvailable,
            preparationAvailable = engineAvailable,
            selectedWriteMode = if (engineAvailable) WriteMode.SWITCH_COMPATIBLE else WriteMode.RAW_EXPERIMENTAL,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(
        cryptoEngineAvailable = engineAvailable, preparationAvailable = engineAvailable,
        selectedWriteMode = if (engineAvailable) WriteMode.SWITCH_COMPATIBLE else WriteMode.RAW_EXPERIMENTAL,
    ))

    init {
        viewModelScope.launch {
            val active = keyStore.activeKey()
            if (active != null) keyStatus.update(KeyStatus(
                imported = true,
                candidateAccepted = active.byteSize in setOf(80L, 160L, 540L),
                valid = engine.validateKey(active),
                file = active,
            ))
        }
    }
}
