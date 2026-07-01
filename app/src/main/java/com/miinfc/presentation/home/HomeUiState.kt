package com.miinfc.presentation.home

data class HomeUiState(
    val appName: String = "MiiNFC",
    val hasImportedKeyFile: Boolean = false,
    val hasAcceptedKeyCandidate: Boolean = false,
    val hasValidKey: Boolean = false,
    val hasSelectedAmiibo: Boolean = false,
    val selectedAmiiboName: String? = null,
    val selectedAmiiboSeries: String? = null,
    val selectedAmiiboImageUri: String? = null,
    val cryptoEngineAvailable: Boolean = false,
    val preparationAvailable: Boolean = false,
    val missingBinFilesCount: Int = 0,
    val missingNfcFilesCount: Int = 0,
    val selectedWriteMode: com.miinfc.domain.amiibo.WriteMode = com.miinfc.domain.amiibo.WriteMode.RAW_EXPERIMENTAL,
) {
    val keyStructurallyValid get() = hasValidKey
    val hasKeyLoaded: Boolean get() = hasImportedKeyFile && hasAcceptedKeyCandidate
    val canWriteRaw: Boolean get() = hasKeyLoaded && hasSelectedAmiibo
    val canWriteSwitchCompatible: Boolean get() = hasKeyLoaded && hasSelectedAmiibo && cryptoEngineAvailable && preparationAvailable
    val requiresRawConfirmation: Boolean get() = canWriteRaw && selectedWriteMode == com.miinfc.domain.amiibo.WriteMode.RAW_EXPERIMENTAL
    val canWriteToNfc: Boolean get() = when (selectedWriteMode) {
        com.miinfc.domain.amiibo.WriteMode.RAW_EXPERIMENTAL -> canWriteRaw
        com.miinfc.domain.amiibo.WriteMode.SWITCH_COMPATIBLE -> canWriteSwitchCompatible
    }
    val writeBlockReason: String get() = when {
        !hasImportedKeyFile || !hasAcceptedKeyCandidate -> "Importe um arquivo de chave compatível para continuar."
        !hasSelectedAmiibo -> "Importe ou escaneie um Amiibo para continuar."
        !cryptoEngineAvailable -> "Modo RAW experimental disponível. Compatibilidade Nintendo Switch não garantida."
        !hasValidKey -> "A chave foi reconhecida, mas ainda precisa ser validada pelo motor Amiibo."
        !preparationAvailable -> "A preparação Amiibo ainda não está disponível."
        else -> "Pronto para escrever em uma tag NTAG215."
    }
    val engineStatusTitle: String get() = if (cryptoEngineAvailable) "Motor Amiibo disponível" else "Motor Amiibo não integrado"
    val engineStatusDescription: String get() = if (cryptoEngineAvailable)
        "A preparação segura para NTAG215 está disponível."
    else "A chave e o Amiibo podem estar corretos. Esta versão ainda não contém o componente que prepara os dados para o Nintendo Switch."
}
