package com.miinfc.presentation.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miinfc.domain.amiibo.AmiiboSourceFile
import com.miinfc.domain.repository.AmiiboLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

data class CollectionState(val items: List<AmiiboSourceFile> = emptyList())

@HiltViewModel
class CollectionViewModel @Inject constructor(private val repository: AmiiboLibraryRepository) : ViewModel() {
    val state: StateFlow<List<AmiiboSourceFile>> = repository.amiibos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun select(id: String, done: () -> Unit) = viewModelScope.launch { repository.selectAmiibo(id); done() }
}
