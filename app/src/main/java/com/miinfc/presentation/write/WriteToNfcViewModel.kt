package com.miinfc.presentation.write

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miinfc.domain.amiibo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WriteToNfcViewModel(
    private val engineProvider: AmiiboCryptoEngineProvider,
) : ViewModel() {
    private val mutableState = MutableStateFlow(WriteToNfcUiState(status = AmiiboCompatibilityStatus.MISSING_CRYPTO_ENGINE))
    val state: StateFlow<WriteToNfcUiState> = mutableState.asStateFlow()
    private var key: ImportedKeyFile? = null
    private var source: AmiiboSourceFile? = null
    private var uid: ByteArray? = null
    var preparedImage: PreparedAmiiboImage? = null
        private set

    fun setInputs(keyFile: ImportedKeyFile?, sourceFile: AmiiboSourceFile?, targetUid: ByteArray?) {
        key = keyFile; source = sourceFile; uid = targetUid?.copyOf()
        refreshEngine()
    }

    fun refreshEngine() {
        val engine = engineProvider.get()
        val functional = engine.cryptoPreparationAvailable
        val keyValid = key?.let(engine::validateKey) == true
        val ready = functional && keyValid && source != null && uid?.size == 7
        mutableState.update { old -> old.copy(
            hasImportedKeyFile = key != null, hasValidCryptoKey = keyValid,
            hasSelectedAmiibo = source != null, amiiboFileValid = source != null,
            selectedAmiiboName = source?.displayName,
            selectedAmiiboType = source?.let { if (it.format == AmiiboSourceFormat.RAW_BIN) ".bin" else ".nfc" },
            targetUidRead = uid != null, cryptoEngineAvailable = functional,
            status = when {
                !functional -> AmiiboCompatibilityStatus.MISSING_CRYPTO_ENGINE
                key == null -> AmiiboCompatibilityStatus.MISSING_KEY
                !keyValid -> AmiiboCompatibilityStatus.INVALID_KEY
                source == null -> AmiiboCompatibilityStatus.MISSING_AMIIBO_FILE
                ready -> AmiiboCompatibilityStatus.READY_TO_PREPARE
                else -> AmiiboCompatibilityStatus.NOT_READY
            },
        ) }
    }

    fun prepareForTargetUid() {
        val selectedKey = key ?: return
        val selectedSource = source ?: return
        val target = uid ?: return
        val engine = engineProvider.get()
        viewModelScope.launch {
            val result = AmiiboPreparationUseCase(engine).prepare(selectedKey, selectedSource, target)
            result.onSuccess { image ->
                preparedImage = image
                mutableState.update { it.copy(
                    preparedForTargetUid = true,
                    status = AmiiboCompatibilityStatus.PREPARED_FOR_TARGET_UID,
                    errorMessage = null,
                ) }
            }.onFailure { error ->
                mutableState.update { it.copy(
                    preparedForTargetUid = false,
                    status = when (error) {
                        is AmiiboPreparationException.MissingCryptoEngine -> AmiiboCompatibilityStatus.MISSING_CRYPTO_ENGINE
                        is AmiiboPreparationException.MissingOrInvalidKey -> AmiiboCompatibilityStatus.INVALID_KEY
                        else -> AmiiboCompatibilityStatus.NOT_READY
                    },
                    errorMessage = error.message,
                ) }
            }
        }
    }
}
