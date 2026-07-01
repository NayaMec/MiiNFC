package com.miinfc.presentation.home

import org.junit.Assert.*
import org.junit.Test

class HomeUiStateTest {
    @Test fun `raw mode is active when key and Amiibo exist without engine`() {
        val state = HomeUiState(
            hasImportedKeyFile = true, hasValidKey = true, hasSelectedAmiibo = true,
            hasAcceptedKeyCandidate = true,
            cryptoEngineAvailable = false, preparationAvailable = false,
        )
        assertTrue(state.canWriteRaw)
        assertTrue(state.canWriteToNfc)
        assertTrue(state.requiresRawConfirmation)
        assertFalse(state.canWriteSwitchCompatible)
    }

    @Test fun `raw mode requires both loaded key and selected Amiibo`() {
        assertFalse(HomeUiState(hasSelectedAmiibo = true).canWriteRaw)
        assertFalse(HomeUiState(hasImportedKeyFile = true, hasAcceptedKeyCandidate = true).canWriteRaw)
    }

    @Test fun `write requires all three guided stages and preparation`() {
        val state = HomeUiState(
            hasImportedKeyFile = true, hasValidKey = true, hasSelectedAmiibo = true,
            hasAcceptedKeyCandidate = true,
            cryptoEngineAvailable = true, preparationAvailable = true,
        )
        assertTrue(state.canWriteToNfc)
    }

    @Test fun `missing key and Amiibo have actionable reasons`() {
        assertEquals("Importe um arquivo de chave compatível para continuar.", HomeUiState().writeBlockReason)
        assertEquals("Importe ou escaneie um Amiibo para continuar.",
            HomeUiState(hasImportedKeyFile = true, hasAcceptedKeyCandidate = true, hasValidKey = true).writeBlockReason)
    }
}
