package com.miinfc.presentation.diagnostic

import androidx.lifecycle.ViewModel
import com.miinfc.domain.repository.RawWriteStatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiagnosticViewModel @Inject constructor(repository: RawWriteStatusRepository) : ViewModel() {
    val rawReport = repository.report
}
