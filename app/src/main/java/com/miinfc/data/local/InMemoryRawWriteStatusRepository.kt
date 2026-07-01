package com.miinfc.data.local

import com.miinfc.domain.repository.*
import kotlinx.coroutines.flow.*

class InMemoryRawWriteStatusRepository : RawWriteStatusRepository {
    private val mutable = MutableStateFlow(RawWriteDiagnosticSnapshot())
    override val report = mutable.asStateFlow()
    override fun set(report: RawWriteDiagnosticSnapshot) { mutable.value = report }
}
