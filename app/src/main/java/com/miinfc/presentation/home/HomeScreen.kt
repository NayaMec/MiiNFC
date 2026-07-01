package com.miinfc.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miinfc.presentation.components.*
import com.miinfc.domain.amiibo.WriteMode

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onImportKey: () -> Unit,
    onKeyGuide: () -> Unit,
    onImportBin: () -> Unit,
    onScanAmiibo: () -> Unit,
    onBinGuide: () -> Unit,
    onAmiiboInfo: () -> Unit,
    onWrite: () -> Unit,
    onWriteGuide: () -> Unit,
    onCollection: () -> Unit,
    onSettings: () -> Unit,
) {
    var showRawConfirmation by remember { mutableStateOf(false) }
    if (showRawConfirmation) AlertDialog(
        onDismissRequest = { showRawConfirmation = false },
        title = { Text("Gravação RAW experimental") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Esta gravação será feita sem preparação Amiibo para o UID da tag. A tag pode não ser reconhecida pelo Nintendo Switch. Nenhum lock permanente será aplicado nesta versão.")
            Text("✓ Chave carregada")
            Text("✓ Arquivo Amiibo selecionado")
            Text("• Tag NTAG215 será necessária")
            Text("• Locks permanentes: não serão aplicados")
            Text("• Compatibilidade Nintendo Switch: não garantida")
        } },
        confirmButton = { Button(onClick = { showRawConfirmation = false; onWrite() }) { Text("Continuar mesmo assim") } },
        dismissButton = { TextButton(onClick = { showRawConfirmation = false }) { Text("Cancelar") } },
    )
    Scaffold(bottomBar = { MiiNfcBottomBar("home", {}, onCollection, onSettings) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).statusBarsPadding().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(uiState.appName, style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            item {
                MiiNfcSection("1. Chave") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MiiNfcActionButton("🔑  Importar arquivo de chave", onImportKey, modifier = Modifier.weight(1f))
                        MiiNfcGuideLink(onKeyGuide)
                    }
                    Text("Toque no botão de ajuda para saber mais.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    StatusText(when {
                        uiState.hasValidKey -> "Chave válida"
                        uiState.hasAcceptedKeyCandidate && !uiState.cryptoEngineAvailable -> "Arquivo de chave reconhecido"
                        uiState.hasAcceptedKeyCandidate -> "Arquivo de chave reconhecido · validação pendente"
                        uiState.hasImportedKeyFile -> "Arquivo selecionado, mas formato não reconhecido"
                        else -> "Nenhuma chave importada"
                    }, uiState.hasValidKey || uiState.hasAcceptedKeyCandidate)
                }
            }
            item {
                MiiNfcSection("2. Amiibo") {
                    MiiNfcActionButton("📄  Importar arquivo .bin", onImportBin)
                    OrDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MiiNfcActionButton("⌗  Escanear Amiibo existente", onScanAmiibo, modifier = Modifier.weight(1f))
                        MiiNfcGuideLink(onBinGuide)
                    }
                    Text("O botão .bin aceita backups binários e arquivos Flipper .nfc, ou você pode escanear um Amiibo existente.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                if (uiState.hasSelectedAmiibo) SelectedAmiiboCard(
                    uiState.selectedAmiiboName ?: "Amiibo", uiState.selectedAmiiboSeries,
                    uiState.selectedAmiiboImageUri, onAmiiboInfo, onCollection,
                ) else Text("Nenhum Amiibo carregado.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            item {
                MiiNfcSection("3. Escrita") {
                    EngineStatusCard(uiState)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MiiNfcActionButton(
                            text = uiState.selectedAmiiboName?.let { "📡  Escrever para NFC: $it" } ?: "📡  Escrever para NFC",
                            onClick = {
                                if (uiState.requiresRawConfirmation) showRawConfirmation = true
                                else onWrite()
                            }, enabled = uiState.canWriteToNfc, modifier = Modifier.weight(1f),
                        )
                        MiiNfcGuideLink(onWriteGuide)
                    }
                    if (!uiState.canWriteToNfc) Text(uiState.writeBlockReason,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else if (uiState.selectedWriteMode == WriteMode.RAW_EXPERIMENTAL) Text(
                        "Modo RAW experimental. Compatibilidade Nintendo Switch não garantida.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Uma tag só pode ser escrita com UM Amiibo. Ela será bloqueada depois.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable private fun EngineStatusCard(state: HomeUiState) {
    val available = state.cryptoEngineAvailable
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (available) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${if (available) "✓" else "!"} ${state.engineStatusTitle}", style = MaterialTheme.typography.titleMedium)
            Text(state.engineStatusDescription, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun OrDivider() {
    Text("OU", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
@Composable private fun StatusText(text: String, ok: Boolean) {
    Text("${if (ok) "✓" else "○"} $text", color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
}
