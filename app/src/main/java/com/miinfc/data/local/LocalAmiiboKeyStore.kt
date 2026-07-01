package com.miinfc.data.local

import android.content.Context
import com.miinfc.domain.amiibo.AmiiboKeyStore
import com.miinfc.domain.amiibo.AmiiboPreparationException
import com.miinfc.domain.amiibo.ImportedKeyFile
import java.io.File
import java.util.UUID

/** Stores user-provided key material in app-private storage; key bytes are never logged or exposed. */
class LocalAmiiboKeyStore(private val context: Context) : AmiiboKeyStore {
    private val prefs by lazy { context.getSharedPreferences("amiibo_key_store", Context.MODE_PRIVATE) }

    override suspend fun import(displayName: String, bytes: ByteArray): Result<ImportedKeyFile> = runCatching {
        if (bytes.isEmpty() || bytes.size > 65_536) throw AmiiboPreparationException.InvalidKey()
        val id = UUID.randomUUID().toString()
        val file = File(context.filesDir, "keys/$id.key")
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        prefs.edit().putString("active_id", id).putString("active_name", displayName).apply()
        ImportedKeyFile(id, displayName, file.absolutePath, bytes.size.toLong())
    }

    override suspend fun read(file: ImportedKeyFile): Result<ByteArray> = runCatching {
        val expectedRoot = File(context.filesDir, "keys").canonicalFile
        val target = File(file.privatePath).canonicalFile
        if (!target.path.startsWith(expectedRoot.path + File.separator) || !target.isFile) throw AmiiboPreparationException.InvalidKey()
        target.readBytes().also { if (it.isEmpty() || it.size > 65_536) throw AmiiboPreparationException.InvalidKey() }
    }

    override suspend fun activeKey(): ImportedKeyFile? {
        val id = prefs.getString("active_id", null) ?: return null
        val name = prefs.getString("active_name", null) ?: "Chave importada"
        val file = File(context.filesDir, "keys/$id.key")
        val size = file.takeIf { it.isFile }?.length() ?: 0
        return ImportedKeyFile(id, name, file.absolutePath, size,
            detectedType = if (size == 540L) com.miinfc.domain.amiibo.KeyFileDetectedType.FLIPPER_NFC_DEVICE else com.miinfc.domain.amiibo.KeyFileDetectedType.RAW_BINARY,
            pageCount = if (size == 540L) 135 else null)
    }
}
