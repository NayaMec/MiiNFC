package com.miinfc.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MiiNfcBottomBar(selected: String, onHome: () -> Unit, onCollection: () -> Unit, onSettings: () -> Unit) {
    Surface(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), shape = RoundedCornerShape(32.dp),
        tonalElevation = 4.dp, shadowElevation = 8.dp) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            NavigationBarItem(selected == "home", onHome, icon = { Text("◉") }, label = { Text("MiiNFC") })
            NavigationBarItem(selected == "collection", onCollection, icon = { Text("◆") }, label = { Text("Coleção") })
            NavigationBarItem(selected == "settings", onSettings, icon = { Text("⚙") }, label = { Text("Configurações") })
        }
    }
}
