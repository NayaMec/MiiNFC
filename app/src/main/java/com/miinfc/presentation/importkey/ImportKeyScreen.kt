package com.miinfc.presentation.importkey

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.miinfc.domain.amiibo.ImportKeyState

@Composable
fun ImportKeyScreen(state: ImportKeyState, onFileRead: (String, ByteArray) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Arquivo vazio")
            val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0) else null
            } ?: "chave-local"
            onFileRead(name, bytes)
        }
    }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Importar chave Amiibo", style = MaterialTheme.typography.headlineSmall)
        Text("Arquivo selecionado: ${if (state.fileSelected) "sim" else "não"}")
        state.fileName?.let { Text("Nome: $it") }
        Text("Tipo detectado: ${when (state.detectedType) {
            com.miinfc.domain.amiibo.KeyFileDetectedType.FLIPPER_NFC_DEVICE -> "Flipper NFC device"
            com.miinfc.domain.amiibo.KeyFileDetectedType.RAW_BINARY -> "Binário bruto"
            com.miinfc.domain.amiibo.KeyFileDetectedType.TEXT_KEY -> "Arquivo de texto"
            com.miinfc.domain.amiibo.KeyFileDetectedType.UNSUPPORTED -> "Não suportado"
            else -> "Desconhecido"
        }}")
        Text("Formato lido: ${if (state.readableFormat) "sim" else "não"}")
        Text("Candidato a chave: ${if (state.acceptedAsKeyCandidate) "sim" else "não"}")
        Text("Formato da chave: ${if (state.keyStructurallyValid) "válido" else "não validado"}")
        Text("Motor Amiibo: ${if (state.cryptoEngineAvailable) "integrado" else "não integrado"}")
        state.message?.let { Text(it, color = if (state.readableFormat) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error) }
        Button(onClick = { launcher.launch(arrayOf("application/octet-stream", "text/plain", "*/*")) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(if (state.fileSelected) "Escolher outro arquivo de chave" else "Escolher arquivo de chave") }
        Text("O conteúdo da chave nunca é exibido ou registrado.", style = MaterialTheme.typography.bodySmall)
        if (state.keyStructurallyValid) Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Continuar") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
    }
}
