package com.miinfc.domain.repository

import kotlinx.coroutines.flow.StateFlow

enum class RawPhysicalWriteStatus { NOT_PERFORMED, COMPLETED, FAILED }
data class RawWriteDiagnosticSnapshot(
    val status: RawPhysicalWriteStatus = RawPhysicalWriteStatus.NOT_PERFORMED,
    val targetUidRead: Boolean = false,
    val targetUidLengthValid: Boolean = false,
    val ntag215Detected: Boolean = false,
    val tagWritable: Boolean = false,
    val pagesVerified: Boolean = false,
    val keyUsedInPreparation: Boolean = false,
    val preparedForTargetUid: Boolean = false,
    val pwdPackConfigApplied: Boolean = false,
    val locksApplied: Boolean = false,
    val switchCompatible: Boolean = false,
)
interface RawWriteStatusRepository {
    val report: StateFlow<RawWriteDiagnosticSnapshot>
    fun set(report: RawWriteDiagnosticSnapshot)
}
