package com.spectra.logger.feature.logs.storage

import com.spectra.logger.core.model.*
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogFilter
import com.spectra.logger.core.storage.FileSystem
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
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
 * - Asynchronous batched writes for < 0.1ms logging overhead
 *
 * @param fileSystem Platform-specific file system implementation
 * @param maxFileSize Maximum size per log file in bytes (default 10MB)
 * @param maxFiles Maximum number of log files to keep (default 5)
 * @param flushThreshold Number of logs to accumulate before async writing
 */
class FileLogStorage(
    private val fileSystem: FileSystem,
    private val maxFileSize: Long = DEFAULT_MAX_FILE_SIZE,
    private val maxFiles: Int = DEFAULT_MAX_FILES,
    private val flushThreshold: Int = 50,
    private val maxCapacity: Int = InMemoryLogStorage.DEFAULT_CAPACITY,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : LogStorage {
    private val json = Json { prettyPrint = false }
    private val logFlow = MutableSharedFlow<LogEntry>(replay = 0, extraBufferCapacity = 64)

    private val lock = SynchronizedObject()
    private val buffer = ArrayDeque<LogEntry>(maxCapacity)
    private val pendingWrites = mutableListOf<LogEntry>()
    private val countAtomic = atomic(0)

    private val ioScope = CoroutineScope(backgroundDispatcher + SupervisorJob())
    private var currentFileIndex = 0
    private val initialized = CompletableDeferred<Unit>()

    init {
        ioScope.launch {
            initialize()
            initialized.complete(Unit)
        }
    }

    private val currentFileName: String
        get() = "logs_$currentFileIndex.jsonl"

    override suspend fun add(entry: LogEntry) {
        var shouldFlush = false
        var batchToWrite: List<LogEntry>? = null

        synchronized(lock) {
            if (buffer.size >= maxCapacity) {
                buffer.removeFirst()
            }
            buffer.addLast(entry)

            pendingWrites.add(entry)
            if (pendingWrites.size >= flushThreshold) {
                batchToWrite = pendingWrites.toList()
                pendingWrites.clear()
                shouldFlush = true
            }
        }

        logFlow.emit(entry)

        if (shouldFlush && batchToWrite != null) {
            flushBatch(batchToWrite!!)
        }
    }

    override suspend fun addAll(entries: List<LogEntry>) {
        if (entries.isEmpty()) return

        var batchToWrite: List<LogEntry>? = null

        synchronized(lock) {
            entries.forEach { entry ->
                if (buffer.size >= maxCapacity) {
                    buffer.removeFirst()
                }
                buffer.addLast(entry)
                pendingWrites.add(entry)
            }

            if (pendingWrites.size >= flushThreshold) {
                batchToWrite = pendingWrites.toList()
                pendingWrites.clear()
            }
        }

        entries.forEach { logFlow.emit(it) }

        if (batchToWrite != null) {
            flushBatch(batchToWrite!!)
        }
    }

    private fun flushBatch(batch: List<LogEntry>) {
        ioScope.launch {
            try {
                performWrite(batch)
            } catch (e: Exception) {
                // Ignore I/O errors to prevent crashing the host app
            }
        }
    }

    private suspend fun performWrite(batch: List<LogEntry>) {
        initialized.await()
        val lines = batch.joinToString("") { json.encodeToString(it) + "\n" }
        
        val currentSize = fileSystem.getFileSize(currentFileName)
        if (currentSize + lines.length > maxFileSize) {
            rotateFiles()
        }
        
        fileSystem.writeText(currentFileName, lines, append = true)
        countAtomic.addAndGet(batch.size)
    }

    suspend fun flush() {
        var batchToWrite: List<LogEntry>? = null
        synchronized(lock) {
            if (pendingWrites.isNotEmpty()) {
                batchToWrite = pendingWrites.toList()
                pendingWrites.clear()
            }
        }
        if (batchToWrite != null) {
            withContext(backgroundDispatcher) {
                try {
                    performWrite(batchToWrite!!)
                } catch (e: Exception) {}
            }
        }
    }

    override suspend fun query(
        filter: LogFilter,
        limit: Int?,
    ): List<LogEntry> {
        flush()
        initialized.await()
        
        return withContext(backgroundDispatcher) {
            val allLogs = mutableListOf<LogEntry>()

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
                                null
                            }
                        }
                        .reversed()

                allLogs.addAll(logs)
            }

            val filtered = allLogs.filter { filter.matches(it) }

            if (limit != null && limit > 0) {
                filtered.take(limit)
            } else {
                filtered
            }
        }
    }

    override fun observe(filter: LogFilter): Flow<LogEntry> = logFlow.filter { filter.matches(it) }

    override suspend fun clear() {
        synchronized(lock) {
            buffer.clear()
            pendingWrites.clear()
            countAtomic.value = 0
        }
        withContext(backgroundDispatcher) {
            try {
                for (i in 0..currentFileIndex) {
                    fileSystem.delete("logs_$i.jsonl")
                }
                currentFileIndex = 0
            } catch (e: Exception) {}
        }
    }

    override suspend fun count(): Int {
        return countAtomic.value
    }

    private suspend fun rotateFiles() {
        currentFileIndex++
        val oldestFileIndex = currentFileIndex - maxFiles
        if (oldestFileIndex >= 0) {
            fileSystem.delete("logs_$oldestFileIndex.jsonl")
        }
    }

    private suspend fun initialize() {
        withContext(backgroundDispatcher) {
            try {
                val files = fileSystem.listFiles(".")
                val logFiles = files.filter { it.startsWith("logs_") && it.endsWith(".jsonl") }
                val indices =
                    logFiles.mapNotNull { fileName ->
                        fileName.removePrefix("logs_").removeSuffix(".jsonl").toIntOrNull()
                    }

                currentFileIndex = indices.maxOrNull() ?: 0
                
                var total = 0
                for (i in currentFileIndex downTo maxOf(0, currentFileIndex - maxFiles + 1)) {
                    val fileName = "logs_$i.jsonl"
                    if (!fileSystem.exists(fileName)) continue
                    val content = fileSystem.readText(fileName) ?: continue
                    total += content.lines().count { it.isNotBlank() }
                }
                countAtomic.value = total
            } catch (e: Exception) {}
        }
    }

    companion object {
        const val DEFAULT_MAX_FILE_SIZE = 10_485_760L // 10MB
        const val DEFAULT_MAX_FILES = 5
    }
}
