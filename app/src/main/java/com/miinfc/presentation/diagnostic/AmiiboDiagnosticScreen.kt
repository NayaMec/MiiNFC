package com.miinfc.presentation.diagnostic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.miinfc.domain.amiibo.AmiiboDiagnosticReport

private enum class DiagnosticStatus { OK, PENDING, WARNING, ERROR }
private data class DiagnosticItem(val label: String, val value: String, val status: DiagnosticStatus)

@Composable
fun AmiiboDiagnosticScreen(report: AmiiboDiagnosticReport, onBack: () -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("Diagnóstico Amiibo", style = MaterialTheme.typography.headlineLarge)
            Text("Veja o que está impedindo a escrita compatível com Nintendo Switch.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!report.engineFunctional) item { EngineAlertCard() }
        item { DiagnosticCard("Ambiente", listOf(
            item("NFC disponível", report.nfcAvailable), item("NFC ativado", report.nfcEnabled),
            item("Celular real", report.isPhysicalDevice))) }
        item { DiagnosticCard("Arquivos", listOf(
            item("Chave importada", report.keyImported), item("Chave válida", report.keyValidated),
            item("Arquivo Amiibo selecionado", report.fileName != null), item("Arquivo normalizado", report.fileNormalized))) }
        item { DiagnosticCard("Motor Amiibo", listOf(
            item("Engine registrado", report.engineRegistered), item("Engine funcional", report.engineFunctional, error = true),
            item("Motor criptográfico disponível", report.cryptoEngineAvailable, error = true),
            item("Chave usada na preparação", report.keyUsedInPreparation),
            item("Dados preparados para UID alvo", report.preparedForTargetUid))) }
        item { DiagnosticCard("Tag NFC", listOf(
            item("NTAG215 detectada", report.isNtag215), item("UID da tag lido", report.targetUidRead),
            item("Tag vazia", report.tagBlankBeforeWrite), item("Tag gravável", report.tagWritable))) }
        item { DiagnosticCard("Escrita", listOf(
            item("Dados preparados", report.preparedForTargetUid), item("Escrita física concluída", report.physicalWriteOk),
            item("PWD / PACK / config aplicados", report.pwdPackConfigApplied),
            item("Locks aplicados", report.locksApplied), item("Verificação concluída", report.verificationOk))) }
        item { DiagnosticCard("Resultado Nintendo", listOf(
            DiagnosticItem("Escrita física RAW", report.rawPhysicalWriteStatus,
                if (report.rawPhysicalWriteStatus == "Concluída") DiagnosticStatus.OK else if (report.rawPhysicalWriteStatus == "Falhou") DiagnosticStatus.ERROR else DiagnosticStatus.PENDING),
            item("Compatível com Nintendo Switch", report.switchCompatible, error = true))) }
        if (report.rawPhysicalWriteStatus == "Concluída" && !report.switchCompatible) item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text("Para ser reconhecida pelo Switch, a tag precisa ser preparada com key + dump + UID da tag.",
                    modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        report.failureReason?.takeIf { report.engineFunctional }?.let { reason ->
            item { Text("Motivo: $reason", color = MaterialTheme.colorScheme.error) }
        }
        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(28.dp)) { Text("Voltar") }
        }
    }
}

@Composable private fun EngineAlertCard() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Motor Amiibo não implementado", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onErrorContainer)
            Text("A escrita Switch-compatible está bloqueada até que um AmiiboCryptoEngine funcional seja integrado.",
                color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable private fun DiagnosticCard(title: String, items: List<DiagnosticItem>) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            items.forEach { entry ->
                val color = when (entry.status) {
                    DiagnosticStatus.OK -> Color(0xFF238636)
                    DiagnosticStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                    DiagnosticStatus.WARNING -> Color(0xFFB65C00)
                    DiagnosticStatus.ERROR -> MaterialTheme.colorScheme.error
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${if (entry.status == DiagnosticStatus.OK) "✓" else if (entry.status == DiagnosticStatus.ERROR) "✕" else "○"} ${entry.label}")
                    Text(entry.value, color = color, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private fun item(label: String, value: Boolean, error: Boolean = false) = DiagnosticItem(
    label, if (value) "OK" else if (error) "Bloqueado" else "Pendente",
    if (value) DiagnosticStatus.OK else if (error) DiagnosticStatus.ERROR else DiagnosticStatus.PENDING,
)
