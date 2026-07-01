package com.miinfc.presentation.write

import android.app.Activity
import android.nfc.NfcAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun RawExperimentalWriteScreen(state: RawWriteState, onTag: (android.nfc.Tag) -> Unit, onDiagnostic: () -> Unit, onBack: () -> Unit) {
    val activity = LocalContext.current as Activity
    val terminal = state is RawWriteState.Success
    DisposableEffect(activity, terminal) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (!terminal) adapter?.enableReaderMode(activity, { onTag(it) },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
        onDispose { adapter?.disableReaderMode(activity) }
    }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Gravação RAW experimental", style = MaterialTheme.typography.headlineSmall)
        Text("Mantenha a NTAG215 encostada até o fim da gravação e verificação.")
        when (state) {
            RawWriteState.WaitingForTag -> { CircularProgressIndicator(); Text("Aguardando tag NTAG215…") }
            RawWriteState.Writing -> { CircularProgressIndicator(); Text("Gravando páginas de usuário e verificando…") }
            is RawWriteState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is RawWriteState.Success -> {
                Text("Gravação física concluída.", style = MaterialTheme.typography.titleLarge)
                Text("Páginas escritas: ${state.pagesWritten}")
                Text("Páginas verificadas: ${state.pagesVerified}")
                Text("Compatibilidade Nintendo Switch: não garantida.", color = MaterialTheme.colorScheme.error)
            }
        }
        Text("UID, password, configuração, PACK e lock bytes não são modificados.", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = onDiagnostic, modifier = Modifier.fillMaxWidth()) { Text("Diagnóstico Amiibo") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
    }
}
