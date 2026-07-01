package com.miinfc.presentation.write

import com.miinfc.domain.amiibo.AmiiboCompatibilityStatus
import org.junit.Assert.*
import org.junit.Test

class WriteToNfcUiStateTest {
    @Test fun `switch write stays blocked without crypto engine`() {
        val state = WriteToNfcUiState(
            hasImportedKeyFile = true, hasValidCryptoKey = true,
            hasSelectedAmiibo = true, amiiboFileValid = true, nfcEnabled = true,
            cryptoEngineAvailable = false,
        )
        assertFalse(state.canStartSwitchWrite)
        assertFalse(state.switchCompatible)
    }

    @Test fun `physical write alone is never Switch compatible`() {
        val state = WriteToNfcUiState(
            physicalWriteComplete = true,
            status = AmiiboCompatibilityStatus.PHYSICALLY_WRITTEN,
        )
        assertFalse(state.switchCompatible)
    }

    @Test fun `real prepared image state is required before write`() {
        val base = WriteToNfcUiState(
            hasImportedKeyFile = true, hasValidCryptoKey = true,
            hasSelectedAmiibo = true, amiiboFileValid = true,
            cryptoEngineAvailable = true, nfcEnabled = true,
        )
        assertFalse(base.canWriteToNfc)
        assertTrue(base.copy(preparedForTargetUid = true).canWriteToNfc)
    }

    @Test fun `primary block reason reports one actionable issue`() {
        assertEquals("Importe uma chave válida para continuar.", getPrimaryBlockReason(WriteToNfcUiState()))
        assertEquals(
            "Motor Amiibo indisponível. A escrita para Switch está bloqueada.",
            getPrimaryBlockReason(WriteToNfcUiState(
                hasValidCryptoKey = true, hasSelectedAmiibo = true, amiiboFileValid = true,
            )),
        )
    }
}
