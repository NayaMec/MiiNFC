package com.miinfc.data.local

import com.miinfc.domain.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryKeyStatusRepository : KeyStatusRepository {
    private val mutable = MutableStateFlow(KeyStatus())
    override val status: StateFlow<KeyStatus> = mutable.asStateFlow()
    override fun update(status: KeyStatus) { mutable.value = status }
}
