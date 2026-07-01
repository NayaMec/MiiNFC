package com.miinfc.presentation.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable fun MiiNfcGuideLink(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text("Guia") }
}
