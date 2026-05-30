package com.spectra.logger.core.storage

import com.spectra.logger.core.utils.*
import com.spectra.logger.core.model.SourceType
import com.spectra.logger.feature.network.model.NetworkLogFilter
import com.spectra.logger.core.model.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android implementation of FileSystem using provided directory path.
 */
actual class FileSystem actual constructor(private val directoryPath: String) {
    private val baseDir: File
        get() =
            File(directoryPath).apply {
                if (!exists()) mkdirs()
            }

    private fun getFile(path: String): File = File(baseDir, path)

    actual suspend fun writeText(
        path: String,
        content: String,
        append: Boolean,
    ) = withContext(Dispatchers.IO) {
        val file = getFile(path)
        file.parentFile?.mkdirs()
        if (append) {
            file.appendText(content)
        } else {
            file.writeText(content)
        }
    }

    actual suspend fun readText(path: String): String? =
        withContext(Dispatchers.IO) {
            val file = getFile(path)
            if (file.exists()) file.readText() else null
        }

    actual suspend fun exists(path: String): Boolean =
        withContext(Dispatchers.IO) {
            getFile(path).exists()
        }

    actual suspend fun delete(path: String): Boolean =
        withContext(Dispatchers.IO) {
            val file = getFile(path)
            file.exists() && file.delete()
        }

    actual suspend fun getFileSize(path: String): Long =
        withContext(Dispatchers.IO) {
            val file = getFile(path)
            if (file.exists()) file.length() else 0L
        }

    actual suspend fun listFiles(path: String): List<String> =
        withContext(Dispatchers.IO) {
            val dir = getFile(path)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.map { it.name } ?: emptyList()
            } else {
                emptyList()
            }
        }

    actual fun getAbsolutePath(path: String): String = getFile(path).absolutePath
}
