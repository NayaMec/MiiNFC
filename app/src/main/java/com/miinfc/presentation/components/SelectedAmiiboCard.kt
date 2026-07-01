package com.miinfc.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectedAmiiboCard(name: String, series: String?, imageUri: String?, onInfo: () -> Unit, onChange: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(Modifier.size(58.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text(if (imageUri == null) "Mii" else "IMG") }
            }
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(series ?: "Série não informada", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column {
                TextButton(onClick = onInfo) { Text("Informação") }
                TextButton(onClick = onChange) { Text("Trocar Amiibo") }
            }
        }
    }
}
