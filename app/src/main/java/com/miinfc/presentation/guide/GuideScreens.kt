package com.miinfc.presentation.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable fun KeyGuideScreen(onFilesHelp: () -> Unit, onBack: () -> Unit) = GuideScreen(
    "Arquivo Chave",
    """Este aplicativo não fornece o arquivo-chave pronto para uso. Você precisará importá-lo por conta própria.

Se você tiver um arquivo de chave compatível, como key_retail.bin, salve-o no aplicativo Arquivos ou selecione-o pelo gerenciador de arquivos para que o MiiNFC possa acessá-lo.

O MiiNFC não baixa, fornece ou compartilha arquivos de chave.""",
    "Como usar o aplicativo Arquivos?" to onFilesHelp, onBack,
)

@Composable fun BinGuideScreen(onImportHelp: () -> Unit, onBack: () -> Unit) = GuideScreen(
    "Arquivos BIN",
    """O MiiNFC não fornece arquivos .bin.

Arquivos .bin são backups brutos de Amiibos físicos. Use apenas arquivos que você possui.

Se você tiver arquivos .bin, importe-os pelo botão 'Importar arquivo .bin'.""",
    "Como importar arquivos?" to onImportHelp, onBack,
)

@Composable fun NfcWriteGuideScreen(onDiagnostic: () -> Unit, onBack: () -> Unit) = GuideScreen(
    "Escrever para Tags",
    """Você pode escrever em uma tag quando as etapas principais estiverem concluídas:

1. Chave válida importada
2. Amiibo carregado
3. Motor Amiibo disponível

Uma vez que os dados do Amiibo são gravados e a tag é finalizada, ela não poderá mais ser regravada. Use tags NTAG215 novas e vazias.""",
    "Ver diagnóstico Amiibo" to onDiagnostic, onBack,
)

@Composable private fun GuideScreen(title: String, body: String, action: Pair<String, () -> Unit>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← Voltar") } }
        Text(title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = action.second, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(action.first) }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Voltar") }
    }
}
