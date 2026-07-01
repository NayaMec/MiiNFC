package com.miinfc.domain.repository

import com.miinfc.domain.amiibo.AmiiboSourceFile
import kotlinx.coroutines.flow.Flow

interface AmiiboLibraryRepository {
    val amiibos: Flow<List<AmiiboSourceFile>>
    val selected: Flow<AmiiboSourceFile?>
    suspend fun save(source: AmiiboSourceFile)
    suspend fun selectAmiibo(id: String)
    suspend fun clearSelectedAmiibo()
}

interface SelectedAmiiboRepository {
    fun getSelectedAmiibo(): Flow<AmiiboSourceFile?>
    suspend fun selectAmiibo(id: String)
    suspend fun clearSelectedAmiibo()
}
