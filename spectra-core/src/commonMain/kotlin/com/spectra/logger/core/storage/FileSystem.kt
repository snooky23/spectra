package com.spectra.logger.core.storage

import com.spectra.logger.core.model.*
import com.spectra.logger.core.utils.*
import com.spectra.logger.core.utils.ioDispatcher
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

/**
 * Cross-platform file system abstraction for log persistence, backed by Okio.
 *
 * This wrapper provides coroutine-safe operations over Okio's synchronous APIs.
 */
class FileSystem(
    val directoryPath: String,
    val okioFs: okio.FileSystem? = defaultFileSystem,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = ioDispatcher,
) {
    /**
     * Write text content to a file.
     *
     * @param path File path relative to app storage directory
     * @param content Text content to write
     * @param append If true, append to existing file; if false, overwrite
     */
    suspend fun writeText(
        path: String,
        content: String,
        append: Boolean = false,
    ) = withContext(dispatcher) {
        val fs = okioFs ?: return@withContext
        val fullPath = getAbsolutePath(path).toPath()
        val parent = fullPath.parent
        if (parent != null && !fs.exists(parent)) {
            fs.createDirectories(parent)
        }
        val sink = if (append) fs.appendingSink(fullPath) else fs.sink(fullPath)
        sink.buffer().use { it.writeUtf8(content) }
    }

    /**
     * Read text content from a file.
     *
     * @param path File path relative to app storage directory
     * @return File content as string, or null if file doesn't exist
     */
    suspend fun readText(path: String): String? =
        withContext(dispatcher) {
            val fs = okioFs ?: return@withContext null
            val fullPath = getAbsolutePath(path).toPath()
            if (!fs.exists(fullPath)) return@withContext null
            try {
                fs.source(fullPath).buffer().use { it.readUtf8() }
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Count non-blank lines in a file efficiently without loading it fully into memory.
     *
     * @param path File path relative to app storage directory
     * @return Number of non-blank lines, or 0 if file doesn't exist
     */
    suspend fun countLines(path: String): Int =
        withContext(dispatcher) {
            val fs = okioFs ?: return@withContext 0
            val fullPath = getAbsolutePath(path).toPath()
            if (!fs.exists(fullPath)) return@withContext 0
            try {
                fs.source(fullPath).buffer().use { source ->
                    var count = 0
                    while (true) {
                        val line = source.readUtf8Line() ?: break
                        if (line.isNotBlank()) count++
                    }
                    count
                }
            } catch (e: Exception) {
                0
            }
        }

    /**
     * Check if a file exists.
     *
     * @param path File path relative to app storage directory
     * @return True if file exists, false otherwise
     */
    suspend fun exists(path: String): Boolean =
        withContext(dispatcher) {
            val fs = okioFs ?: return@withContext false
            fs.exists(getAbsolutePath(path).toPath())
        }

    /**
     * Delete a file.
     *
     * @param path File path relative to app storage directory
     * @return True if file was deleted, false if it didn't exist
     */
    suspend fun delete(path: String): Boolean =
        withContext(dispatcher) {
            val fs = okioFs ?: return@withContext false
            val fullPath = getAbsolutePath(path).toPath()
            if (fs.exists(fullPath)) {
                fs.delete(fullPath)
                true
            } else {
                false
            }
        }

    /**
     * Get the size of a file in bytes.
     *
     * @param path File path relative to app storage directory
     * @return File size in bytes, or 0 if file doesn't exist
     */
    suspend fun getFileSize(path: String): Long =
        withContext(dispatcher) {
            val fs = okioFs ?: return@withContext 0L
            val fullPath = getAbsolutePath(path).toPath()
            if (fs.exists(fullPath)) {
                fs.metadata(fullPath).size ?: 0L
            } else {
                0L
            }
        }

    /**
     * List all files in a directory.
     *
     * @param path Directory path relative to app storage directory
     * @return List of file names in the directory
     */
    suspend fun listFiles(path: String): List<String> =
        withContext(dispatcher) {
            val fs = okioFs ?: return@withContext emptyList()
            val fullPath = getAbsolutePath(path).toPath()
            if (fs.exists(fullPath)) {
                fs.list(fullPath).map { it.name }
            } else {
                emptyList()
            }
        }

    /**
     * Get the absolute native path for a file.
     *
     * @param path File path relative to app storage directory
     * @return The absolute path as a string
     */
    fun getAbsolutePath(path: String): String {
        return if (directoryPath.isEmpty()) {
            path
        } else {
            if (path.isEmpty()) directoryPath else "$directoryPath/$path"
        }
    }

    /**
     * Appends the contents of sourcePath to destPath efficiently.
     *
     * @param sourcePath Source file path
     * @param destPath Destination file path
     */
    suspend fun appendFile(
        sourcePath: String,
        destPath: String,
    ) = withContext(dispatcher) {
        val fs = okioFs ?: return@withContext
        val src = getAbsolutePath(sourcePath).toPath()
        val dest = getAbsolutePath(destPath).toPath()
        if (!fs.exists(src)) return@withContext
        fs.source(src).buffer().use { source ->
            fs.appendingSink(dest).buffer().use { sink ->
                sink.writeAll(source)
            }
        }
    }
}
