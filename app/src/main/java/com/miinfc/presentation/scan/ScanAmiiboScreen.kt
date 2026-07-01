package com.miinfc.presentation.scan

import android.app.Activity
import android.nfc.NfcAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ScanAmiiboScreen(state: ScanState, onTag: (android.nfc.Tag) -> Unit, onDone: () -> Unit, onBack: () -> Unit) {
    val activity = LocalContext.current as Activity
    DisposableEffect(activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        adapter?.enableReaderMode(activity, { onTag(it) },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
        onDispose { adapter?.disableReaderMode(activity) }
    }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Escanear Amiibo existente", style = MaterialTheme.typography.headlineSmall)
        Text("Mantenha o Amiibo encostado ao celular durante a leitura.")
        when (state) {
            ScanState.Waiting -> Text("Aguardando tag NFC…")
            ScanState.Reading -> { CircularProgressIndicator(); Text("Lendo NTAG215…") }
            is ScanState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is ScanState.Success -> { Text("Amiibo carregado: ${state.name}"); Button(onClick = onDone) { Text("Usar este Amiibo") } }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
    }
}
