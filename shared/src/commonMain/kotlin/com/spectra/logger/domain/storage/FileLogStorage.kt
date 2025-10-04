package com.spectra.logger.domain.storage

import com.spectra.logger.domain.model.LogEntry
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.storage.FileSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * File-based log storage with automatic rotation.
 *
 * Features:
 * - Persists logs to disk across app restarts
 * - Automatic file rotation when size limit is reached
 * - Configurable max file size and file count
 * - JSON serialization for reliable storage
 *
 * @param fileSystem Platform-specific file system implementation
 * @param maxFileSize Maximum size per log file in bytes (default 1MB)
 * @param maxFiles Maximum number of log files to keep (default 5)
 */
class FileLogStorage(
    private val fileSystem: FileSystem,
    private val maxFileSize: Long = DEFAULT_MAX_FILE_SIZE,
    private val maxFiles: Int = DEFAULT_MAX_FILES,
) : LogStorage {
    private val json = Json { prettyPrint = false }
    private val logFlow = MutableSharedFlow<LogEntry>(replay = 0, extraBufferCapacity = 64)

    private var currentFileIndex = 0
    private val currentFileName: String
        get() = "logs_$currentFileIndex.jsonl"

    override suspend fun add(entry: LogEntry) {
        // Emit to flow for real-time observers
        logFlow.emit(entry)

        // Serialize and append to file
        val line = json.encodeToString(entry) + "\n"

        // Check if we need to rotate
        val currentSize = fileSystem.getFileSize(currentFileName)
        if (currentSize + line.length > maxFileSize) {
            rotateFiles()
        }

        // Append to current file
        fileSystem.writeText(currentFileName, line, append = true)
    }

    override suspend fun addAll(entries: List<LogEntry>) {
        entries.forEach { entry ->
            add(entry)
        }
    }

    override suspend fun query(
        filter: LogFilter,
        limit: Int?,
    ): List<LogEntry> {
        val allLogs = mutableListOf<LogEntry>()

        // Read from all log files, newest first
        for (i in currentFileIndex downTo maxOf(0, currentFileIndex - maxFiles + 1)) {
            val fileName = "logs_$i.jsonl"
            if (!fileSystem.exists(fileName)) continue

            val content = fileSystem.readText(fileName) ?: continue
            val logs =
                content
                    .lines()
                    .filter { it.isNotBlank() }
                    .mapNotNull { line ->
                        try {
                            json.decodeFromString<LogEntry>(line)
                        } catch (e: Exception) {
                            null // Skip corrupted entries
                        }
                    }
                    .reversed() // Newest first within file

            allLogs.addAll(logs)
        }

        // Apply filter
        val filtered = allLogs.filter { filter.matches(it) }

        // Apply limit
        return if (limit != null && limit > 0) {
            filtered.take(limit)
        } else {
            filtered
        }
    }

    override fun observe(filter: LogFilter): Flow<LogEntry> = logFlow.filter { filter.matches(it) }

    override suspend fun clear() {
        // Delete all log files
        for (i in 0..currentFileIndex) {
            fileSystem.delete("logs_$i.jsonl")
        }
        currentFileIndex = 0
    }

    override suspend fun count(): Int {
        var total = 0
        for (i in currentFileIndex downTo maxOf(0, currentFileIndex - maxFiles + 1)) {
            val fileName = "logs_$i.jsonl"
            if (!fileSystem.exists(fileName)) continue

            val content = fileSystem.readText(fileName) ?: continue
            total += content.lines().count { it.isNotBlank() }
        }
        return total
    }

    /**
     * Rotate to a new log file.
     * Deletes oldest file if we've reached the max file count.
     */
    private suspend fun rotateFiles() {
        currentFileIndex++

        // Delete oldest file if we've exceeded max files
        val oldestFileIndex = currentFileIndex - maxFiles
        if (oldestFileIndex >= 0) {
            fileSystem.delete("logs_$oldestFileIndex.jsonl")
        }
    }

    /**
     * Initialize by finding the current file index.
     * Scans existing log files and sets index to the highest found.
     */
    suspend fun initialize() {
        // Find highest existing file index
        val files = fileSystem.listFiles(".")
        val logFiles = files.filter { it.startsWith("logs_") && it.endsWith(".jsonl") }
        val indices =
            logFiles.mapNotNull { fileName ->
                fileName.removePrefix("logs_").removeSuffix(".jsonl").toIntOrNull()
            }

        currentFileIndex = indices.maxOrNull() ?: 0
    }

    companion object {
        const val DEFAULT_MAX_FILE_SIZE = 1_048_576L // 1MB
        const val DEFAULT_MAX_FILES = 5
    }
}
