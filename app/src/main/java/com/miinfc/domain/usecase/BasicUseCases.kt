package com.miinfc.domain.usecase

import android.nfc.Tag
import com.miinfc.data.nfc.amiibo.AmiiboTagValidator
import com.miinfc.domain.model.*
import com.miinfc.domain.repository.*
import java.util.UUID

class ValidateDumpUseCase {
    operator fun invoke(file: LocalBinFile): ImportBinError? = when {
        file.bytes.isEmpty() -> ImportBinError.EMPTY_FILE
        !file.name.endsWith(".bin", true) || file.mimeType?.let { it != "application/octet-stream" } == true -> ImportBinError.INVALID_FILE_TYPE
        file.bytes.size !in setOf(520, 532, 540, 572) -> ImportBinError.INVALID_SIZE
        else -> null
    }
}

class ImportBinUseCase(private val validate: ValidateDumpUseCase) {
    operator fun invoke(file: LocalBinFile): ImportBinResult {
        validate(file)?.let { return ImportBinResult.Error(it) }
        return ImportBinResult.Success(AmiiboDump(
            UUID.randomUUID().toString(), file.name, file.bytes.copyOf(), file.bytes.size,
            null, null, null, null, null, System.currentTimeMillis(), true, DumpSource.IMPORTED_FILE,
        ))
    }
}

class GenerateReportUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(type: OperationType, success: Boolean, verified: Boolean = false, errorMessage: String? = null): OperationReport {
        val report = OperationReport(UUID.randomUUID().toString(), type, success = success,
            verified = verified, errorMessage = if (success) null else errorMessage,
            timestamp = System.currentTimeMillis())
        repository.save(report)
        return report
    }
}

class WriteAmiiboUseCase<T>(private val writer: Ntag215Writer<T>, private val reports: GenerateReportUseCase) {
    suspend operator fun invoke(tag: T, dump: AmiiboDump): WriteResult {
        if (!dump.isValid) return WriteResult(false, false, 0, AmiiboWriteError.INVALID_DUMP)
        val result = writer.write(tag, dump, true)
        reports(OperationType.TAG_WRITE, result.success, result.verified, result.error?.name)
        return result
    }
}

class ValidateNtag215UseCase(private val validator: AmiiboTagValidator) {
    operator fun invoke(tag: Tag, base: NfcTagInfo): Ntag215ValidationResult = validator.validate(tag, base)
}
