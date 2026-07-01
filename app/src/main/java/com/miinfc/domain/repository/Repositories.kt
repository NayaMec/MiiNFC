package com.miinfc.domain.repository

import com.miinfc.domain.model.*
import kotlinx.coroutines.flow.Flow

fun interface Ntag215Writer<T> {
    suspend fun write(tag: T, dump: AmiiboDump, verify: Boolean): WriteResult
}

interface ReportRepository {
    suspend fun save(report: OperationReport)
    fun getAll(): Flow<List<OperationReport>>
    suspend fun getById(id: String): OperationReport?
}
