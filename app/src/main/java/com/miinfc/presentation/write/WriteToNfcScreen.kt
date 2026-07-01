package com.miinfc.presentation.write

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.miinfc.domain.amiibo.AmiiboCompatibilityStatus

enum class WriteProgress(val label: String) {
    IDLE("Pronto"), WAITING_FOR_TAG("Aguardando tag"), TAG_DETECTED("Tag detectada"),
    PREPARING("Preparando dados"), WRITING("Escrevendo"), VERIFYING("Verificando"), COMPLETED("Concluído"),
}

data class WriteToNfcUiState(
    val hasImportedKeyFile: Boolean = false,
    val hasValidCryptoKey: Boolean = false,
    val hasSelectedAmiibo: Boolean = false,
    val selectedAmiiboName: String? = null,
    val selectedAmiiboType: String? = null,
    val amiiboFileValid: Boolean = false,
    val nfcEnabled: Boolean = false,
    val ntag215Detected: Boolean = false,
    val targetUidRead: Boolean = false,
    val cryptoEngineAvailable: Boolean = false,
    val preparedForTargetUid: Boolean = false,
    val physicalWriteComplete: Boolean = false,
    val finalizationApplied: Boolean = false,
    val verificationComplete: Boolean = false,
    val status: AmiiboCompatibilityStatus = AmiiboCompatibilityStatus.NOT_READY,
    val errorMessage: String? = null,
    val writeProgress: WriteProgress = WriteProgress.IDLE,
) {
    val switchCompatible get() = status == AmiiboCompatibilityStatus.VERIFIED_SWITCH_COMPATIBLE &&
        preparedForTargetUid && physicalWriteComplete && finalizationApplied && verificationComplete
    val canWriteToNfc get() = hasValidCryptoKey && hasSelectedAmiibo && amiiboFileValid &&
        cryptoEngineAvailable && preparedForTargetUid && nfcEnabled
    val canStartSwitchWrite get() = canWriteToNfc
    val blockReason get() = getPrimaryBlockReason(this)
}

fun getPrimaryBlockReason(state: WriteToNfcUiState): String = when {
    !state.hasValidCryptoKey -> "Importe uma chave válida para continuar."
    !state.hasSelectedAmiibo || !state.amiiboFileValid -> "Selecione um arquivo Amiibo válido antes de escrever."
    !state.cryptoEngineAvailable -> "Motor Amiibo indisponível. A escrita para Switch está bloqueada."
    !state.preparedForTargetUid -> "Prepare os dados para o UID da tag antes de escrever."
    !state.nfcEnabled -> "Ative o NFC para continuar."
    else -> "Pronto para escrever em uma tag NTAG215."
}

@Composable
fun WriteToNfcScreen(
    uiState: WriteToNfcUiState,
    onWriteClick: () -> Unit,
    onDiagnosticClick: () -> Unit,
    onSelectAmiiboClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("Escrever para NFC", style = MaterialTheme.typography.headlineLarge)
            Text("Mantenha a tag NTAG215 encostada até o fim da escrita.", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!uiState.canWriteToNfc) item {
            AppCard {
                Text("Escrita indisponível", style = MaterialTheme.typography.titleLarge)
                Text(uiState.blockReason, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else item {
            AppCard {
                Text(uiState.selectedAmiiboName ?: "Amiibo selecionado", style = MaterialTheme.typography.titleLarge)
                Text("Chave: válida", color = OkGreen)
                Text("Motor Amiibo: disponível", color = OkGreen)
                Text("Status: ${uiState.writeProgress.label}")
                Text("Aproxime uma tag NTAG215 vazia.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Button(
                onClick = onWriteClick, enabled = uiState.canWriteToNfc,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(28.dp),
            ) { Text("Iniciar escrita") }
        }
        item {
            OutlinedButton(onClick = onDiagnosticClick, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(28.dp)) { Text("Diagnóstico Amiibo") }
        }
        item {
            OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(28.dp)) { Text("Voltar para início") }
        }
    }
}

@Composable private fun AmiiboMainStatusCard(state: WriteToNfcUiState) {
    val ready = state.canWriteToNfc
    val engineMissing = !state.cryptoEngineAvailable
    AppCard {
        Text(when { ready -> "Pronto para escrever"; engineMissing -> "Motor Amiibo indisponível"; else -> "Preparação pendente" },
            style = MaterialTheme.typography.titleLarge)
        Text(when { ready -> "Aproxime uma tag NTAG215 vazia para continuar."
            engineMissing -> "A escrita para Nintendo Switch está bloqueada por segurança."
            else -> state.blockReason })
        StatusBadge(if (ready) "Compatível com Switch" else "Não compatível com Switch", ready)
    }
}

private data class Step(val label: String, val ok: Boolean, val blocked: Boolean = false)
@Composable private fun RequiredStepsCard(state: WriteToNfcUiState) {
    val steps = listOf(
        Step("Chave válida", state.hasValidCryptoKey),
        Step("Amiibo selecionado", state.hasSelectedAmiibo && state.amiiboFileValid),
        Step("Motor Amiibo disponível", state.cryptoEngineAvailable, !state.cryptoEngineAvailable),
    )
    AppCard {
        Text("Próximos passos", style = MaterialTheme.typography.titleMedium)
        steps.forEach { step ->
            val color = when { step.ok -> OkGreen; step.blocked -> MaterialTheme.colorScheme.error; else -> PendingOrange }
            Text("${when { step.ok -> "✓"; step.blocked -> "✕"; else -> "○" }} ${step.label}  ·  ${if (step.ok) "OK" else if (step.blocked) "bloqueado" else "pendente"}", color = color)
        }
    }
}

@Composable private fun SelectedAmiiboCard(state: WriteToNfcUiState, onSelect: () -> Unit) {
    AppCard {
        Text("Amiibo selecionado", style = MaterialTheme.typography.titleMedium)
        if (state.hasSelectedAmiibo) {
            Text(state.selectedAmiiboName ?: "Arquivo Amiibo")
            Text("Tipo: ${state.selectedAmiiboType ?: "desconhecido"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (state.amiiboFileValid) "Válido" else "Inválido", color = if (state.amiiboFileValid) OkGreen else MaterialTheme.colorScheme.error)
        } else {
            Text("Nenhum Amiibo selecionado", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onSelect, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Selecionar Amiibo") }
        }
    }
}

@Composable private fun WriteSummaryCard(state: WriteToNfcUiState) = AppCard {
    Text("Resumo", style = MaterialTheme.typography.titleMedium)
    SummaryRow("Escrita física", if (state.physicalWriteComplete) "OK" else "Não realizada", state.physicalWriteComplete)
    SummaryRow("Preparação Amiibo", if (state.preparedForTargetUid) "OK" else "Não disponível", state.preparedForTargetUid)
    SummaryRow("Nintendo Switch", if (state.switchCompatible) "Compatível" else "Não compatível", state.switchCompatible)
}

@Composable private fun SummaryRow(label: String, value: String, ok: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label); Text(value, color = if (ok) OkGreen else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun StatusBadge(text: String, ok: Boolean) {
    Text(text, color = if (ok) OkGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.background(if (ok) OkGreen.copy(alpha = .12f) else MaterialTheme.colorScheme.surfaceVariant,
            RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 7.dp))
}

@Composable private fun AppCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

private val OkGreen = Color(0xFF238636)
private val PendingOrange = Color(0xFFB65C00)
