package com.miinfc.domain.repository

import com.miinfc.domain.amiibo.ImportedKeyFile
import kotlinx.coroutines.flow.StateFlow

data class KeyStatus(
    val imported: Boolean = false,
    val candidateAccepted: Boolean = false,
    val valid: Boolean = false,
    val file: ImportedKeyFile? = null,
)
interface KeyStatusRepository {
    val status: StateFlow<KeyStatus>
    fun update(status: KeyStatus)
}
