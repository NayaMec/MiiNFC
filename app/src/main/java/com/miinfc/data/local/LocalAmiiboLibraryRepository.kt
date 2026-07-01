package com.miinfc.data.local

import android.content.Context
import com.miinfc.domain.amiibo.*
import com.miinfc.domain.repository.*
import kotlinx.coroutines.flow.*
import java.io.File

class LocalAmiiboLibraryRepository(private val context: Context) : AmiiboLibraryRepository, SelectedAmiiboRepository {
    private val prefs = context.getSharedPreferences("amiibo_library", Context.MODE_PRIVATE)
    private val mutableAmiibos = MutableStateFlow(loadAll())
    private val mutableSelected = MutableStateFlow(resolveSelected(mutableAmiibos.value))
    override val amiibos: StateFlow<List<AmiiboSourceFile>> = mutableAmiibos.asStateFlow()
    override val selected: StateFlow<AmiiboSourceFile?> = mutableSelected.asStateFlow()

    override suspend fun save(source: AmiiboSourceFile) {
        val dir = File(context.filesDir, "amiibos").apply { mkdirs() }
        File(dir, "${source.id}.dump").writeBytes(source.bytes)
        prefs.edit()
            .putString("name_${source.id}", source.displayName)
            .putString("format_${source.id}", source.format.name)
            .putStringSet("ids", prefs.getStringSet("ids", emptySet()).orEmpty() + source.id)
            .apply()
        mutableAmiibos.value = loadAll()
    }

    override suspend fun selectAmiibo(id: String) {
        val selected = mutableAmiibos.value.firstOrNull { it.id == id } ?: return
        prefs.edit().putString("selected_id", id).apply()
        mutableSelected.value = selected
    }

    override suspend fun clearSelectedAmiibo() {
        prefs.edit().remove("selected_id").apply()
        mutableSelected.value = null
    }

    override fun getSelectedAmiibo(): Flow<AmiiboSourceFile?> = selected

    private fun loadAll(): List<AmiiboSourceFile> = prefs.getStringSet("ids", emptySet()).orEmpty().mapNotNull { id ->
        val file = File(context.filesDir, "amiibos/$id.dump")
        val name = prefs.getString("name_$id", null) ?: return@mapNotNull null
        val format = runCatching { AmiiboSourceFormat.valueOf(prefs.getString("format_$id", "")!!) }.getOrNull() ?: return@mapNotNull null
        if (!file.isFile) return@mapNotNull null
        AmiiboSourceFile(id, name, format, file.readBytes())
    }.sortedBy { it.displayName.lowercase() }

    private fun resolveSelected(items: List<AmiiboSourceFile>): AmiiboSourceFile? =
        prefs.getString("selected_id", null)?.let { id -> items.firstOrNull { it.id == id } }
}
