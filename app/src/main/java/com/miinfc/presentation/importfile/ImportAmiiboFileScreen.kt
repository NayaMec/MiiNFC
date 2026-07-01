package com.miinfc.presentation.importfile

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ImportAmiiboFileScreen(
    fileType: AmiiboImportFileType,
    state: AmiiboFileImportState,
    onFileRead: (String, ByteArray) -> Unit,
    onUseAmiibo: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val unified = fileType == AmiiboImportFileType.BIN
    val extension = if (unified) ".bin ou .nfc" else ".nfc"
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Não foi possível ler o arquivo.")
            val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0) else null
            } ?: "amiibo.bin"
            onFileRead(name, bytes)
        }
    }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(if (unified) "Importar arquivo Amiibo" else "Importar arquivo .nfc", style = MaterialTheme.typography.headlineSmall)
        Text(if (unified) "Selecione um backup Amiibo .bin ou um dump Flipper .nfc que você possui. O formato será detectado automaticamente."
            else "Selecione um arquivo Flipper NFC de uma NTAG215.")
        Button(onClick = { launcher.launch(arrayOf("application/octet-stream", "text/plain", "*/*")) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(if (unified) "Escolher arquivo .bin ou .nfc" else "Escolher arquivo .nfc") }
        when (state) {
            AmiiboFileImportState.Idle -> Text("Status: nenhum arquivo selecionado")
            AmiiboFileImportState.Loading -> { CircularProgressIndicator(); Text("Validando arquivo…") }
            is AmiiboFileImportState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is AmiiboFileImportState.Success -> {
                Text("Arquivo importado com sucesso", color = MaterialTheme.colorScheme.primary)
                Text("Nome: ${state.source.displayName}")
                Text("Tipo: ${if (state.source.format == com.miinfc.domain.amiibo.AmiiboSourceFormat.RAW_BIN) "BIN" else "NFC / Flipper"}")
                Text(state.detail)
                Text("Status: válido")
                Button(onClick = onUseAmiibo, modifier = Modifier.fillMaxWidth()) { Text("Usar este Amiibo") }
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
    }
}
