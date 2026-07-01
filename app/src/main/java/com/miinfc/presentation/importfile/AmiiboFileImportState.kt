package com.miinfc.presentation.importfile

import com.miinfc.domain.amiibo.AmiiboSourceFile

enum class AmiiboImportFileType { BIN, NFC }
sealed interface AmiiboFileImportState {
    data object Idle : AmiiboFileImportState
    data object Loading : AmiiboFileImportState
    data class Success(val source: AmiiboSourceFile, val detail: String) : AmiiboFileImportState
    data class Error(val message: String) : AmiiboFileImportState
}
