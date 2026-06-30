package com.miinfc.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.miinfc.domain.model.AmiiboDump
import com.miinfc.domain.model.DumpSource
import com.miinfc.domain.model.ImportBinError
import com.miinfc.domain.model.NfcAvailabilityStatus
import com.miinfc.domain.model.OperationReport
import com.miinfc.domain.model.OperationType
import com.miinfc.presentation.home.HomeScreen
import com.miinfc.presentation.home.HomeUiState
import com.miinfc.presentation.importbin.ImportBinScreen
import com.miinfc.presentation.importbin.ImportBinUiState
import com.miinfc.presentation.library.LibraryScreen
import com.miinfc.presentation.report.ReportScreen
import com.miinfc.presentation.write.WriteAmiiboScreen
import com.miinfc.presentation.write.WriteStage
import com.miinfc.presentation.write.WriteUiState
import com.miinfc.presentation.write.ndef.WriteNdefTagScreen
import com.miinfc.presentation.write.ndef.WriteNdefUiState
import com.miinfc.domain.model.NdefPayload
import com.miinfc.domain.model.NdefPayloadType
import org.junit.Rule
import org.junit.Test

class ScreensInstrumentedTest {
    @get:Rule val compose = createComposeRule()

    @Test fun homeScreenOpensCorrectly() {
        compose.setContent { MaterialTheme {
            HomeScreen(
                HomeUiState(nfcStatus = NfcAvailabilityStatus.AVAILABLE_ENABLED),
                {}, {}, {}, {}, {}, {}, {}, {}, {},
            )
        } }
        compose.onNodeWithText("Obter etiquetas NFC").assertIsDisplayed()
        compose.onNodeWithText("Importar arquivo de chave").assertIsDisplayed()
        compose.onNodeWithText("Escanear Amiibo existente").assertIsDisplayed()
        compose.onNodeWithText("Escrever para NFC").assertIsDisplayed()
    }

    @Test fun libraryScreenListsDumps() {
        compose.setContent { MaterialTheme {
            LibraryScreen(listOf(dump()), {}, {}, {})
        } }
        compose.onNodeWithText("backup.bin").assertIsDisplayed()
        compose.onNodeWithText("540 bytes").assertIsDisplayed()
    }

    @Test fun importScreenShowsInvalidFileError() {
        compose.setContent { MaterialTheme {
            ImportBinScreen(ImportBinUiState.Error(ImportBinError.INVALID_SIZE), {}, {})
        } }
        compose.onNodeWithText("Tamanho inválido. O dump esperado possui 540 bytes.")
            .assertIsDisplayed()
    }

    @Test fun reportScreenListsReports() {
        compose.setContent { MaterialTheme {
            ReportScreen(listOf(report()), {}, {})
        } }
        compose.onNodeWithText("TAG_READ").assertIsDisplayed()
        compose.onNodeWithText("Sucesso").assertIsDisplayed()
    }

    @Test fun writeScreenShowsExplicitEnabledActionForValidDump() {
        compose.setContent { MaterialTheme {
            WriteAmiiboScreen(
                WriteUiState(dump(), WriteStage.FileValid, fileValid = true),
                {}, {}, {},
            )
        } }
        compose.onNodeWithText("Escrever na tag NFC").assertIsDisplayed().assertIsEnabled()
    }

    @Test fun writeScreenDisablesActionForInvalidDump() {
        compose.setContent { MaterialTheme {
            WriteAmiiboScreen(
                WriteUiState(dump().copy(isValid = false), WriteStage.Idle, fileValid = false),
                {}, {}, {},
            )
        } }
        compose.onNodeWithText("Arquivo inválido").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test fun ndefWriterShowsSeparateActionAndChecklist() {
        val payload = NdefPayload(NdefPayloadType.URI, "https://example.com", null, 24)
        compose.setContent { MaterialTheme {
            WriteNdefTagScreen(
                WriteNdefUiState(value = payload.value, payload = payload),
                {}, {}, {}, {}, {},
            )
        } }
        compose.onNodeWithText("Escrever na etiqueta NFC").assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithText("○ Tag suporta NDEF").assertIsDisplayed()
    }

    private fun dump() = AmiiboDump(
        "id", "backup.bin", ByteArray(540), 540, null, null, null, null, null, null,
        true, DumpSource.IMPORTED_FILE,
    )

    private fun report() = OperationReport(
        "id", OperationType.TAG_READ, "dump", "backup.bin", "0102", true,
        true, false, null, 0L,
    )
}
