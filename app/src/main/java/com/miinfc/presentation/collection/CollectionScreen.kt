package com.miinfc.presentation.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miinfc.domain.amiibo.*

@Composable
fun CollectionScreen(items: List<AmiiboSourceFile>, onSelect: (String) -> Unit, onImportBin: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Text("Coleção", style = MaterialTheme.typography.headlineLarge); Text("Escolha o Amiibo ativo para escrita.") }
        if (items.isEmpty()) item {
            Text("Nenhum Amiibo importado ainda.")
            Button(onClick = onImportBin, modifier = Modifier.fillMaxWidth()) { Text("Importar arquivo .bin ou .nfc") }
        } else items(items, key = { it.id }) { source ->
            Card(Modifier.fillMaxWidth().clickable { onSelect(source.id) }, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(source.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(if (source.format == AmiiboSourceFormat.RAW_BIN) ".bin" else ".nfc")
                    Text("Status: válido", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar") } }
    }
}
