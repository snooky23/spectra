package com.spectra.logger.feature.logs.storage

import com.spectra.logger.core.model.*
import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.core.utils.ioDispatcher
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogFilter
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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
    private val backgroundDispatcher: CoroutineDispatcher = ioDispatcher,
) : LogStorage {
    private val _ioErrorHandler = atomic<((Throwable) -> Unit)?>(null)
    var ioErrorHandler: ((Throwable) -> Unit)?
        get() = _ioErrorHandler.value
        set(value) {
            _ioErrorHandler.value = value
        }
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
            withContext(backgroundDispatcher) {
                try {
                    performWrite(batch)
                } catch (e: Exception) {
                    ioErrorHandler?.invoke(e)
                }
            }
        }
    }

    private val writeMutex = kotlinx.coroutines.sync.Mutex()

    private suspend fun performWrite(batch: List<LogEntry>) {
        if (batch.isEmpty()) return
        writeMutex.withLock {
            initialized.await()
            val currentFileName = "logs_$currentFileIndex.jsonl"
            var runningSize = fileSystem.getFileSize(currentFileName)
            val builder = StringBuilder()
            var builderLengthBytes = 0
            var currentBatchCount = 0

            for (entry in batch) {
                val line = json.encodeToString(entry) + "\n"
                val lineBytes = line.encodeToByteArray()
                val lineSize = lineBytes.size

                val maxAllowedSize = maxFileSize.coerceIn(1L, Int.MAX_VALUE.toLong())
                if (lineSize > maxAllowedSize) {
                    continue
                }

                if (runningSize + builderLengthBytes + lineSize > maxFileSize && (runningSize > 0L || builderLengthBytes > 0)) {
                    if (builder.isNotEmpty()) {
                        val currentWriteFile = "logs_$currentFileIndex.jsonl"
                        fileSystem.writeText(currentWriteFile, builder.toString(), append = true)
                        countAtomic.addAndGet(currentBatchCount)
                        builder.clear()
                        builderLengthBytes = 0
                        currentBatchCount = 0
                    }
                    rotateFiles()
                    runningSize = 0L // reset since we rotated to a new file
                }
                builder.append(line)
                builderLengthBytes += lineSize
                currentBatchCount++
            }

            if (builder.isNotEmpty()) {
                val currentWriteFile = "logs_$currentFileIndex.jsonl"
                fileSystem.writeText(currentWriteFile, builder.toString(), append = true)
                countAtomic.addAndGet(currentBatchCount)
            }
        }
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
                } catch (e: Exception) {
                    ioErrorHandler?.invoke(e)
                }
            }
        }
    }

    override suspend fun query(
        filter: LogFilter,
        limit: Int?,
    ): List<LogEntry> {
        flush()
        initialized.await()

        return writeMutex.withLock {
            withContext(backgroundDispatcher) {
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
    }

    override suspend fun exportLogs(): String? {
        flush()
        initialized.await()

        return writeMutex.withLock {
            withContext(backgroundDispatcher) {
                val timeStr =
                    com.spectra.logger.core.utils.SpectraTime.now().toString()
                        .replace(":", "-")
                        .replace(".", "-")
                        .replace("T", "_")
                val exportFileName = "export_$timeStr.jsonl"

                // If it exists, delete it first (unlikely due to timestamp)
                if (fileSystem.exists(exportFileName)) {
                    fileSystem.delete(exportFileName)
                }

                // Write all log files chronologically (oldest first)
                for (i in maxOf(0, currentFileIndex - maxFiles + 1)..currentFileIndex) {
                    val fileName = "logs_$i.jsonl"
                    if (!fileSystem.exists(fileName)) continue

                    fileSystem.appendFile(fileName, exportFileName)
                }

                fileSystem.getAbsolutePath(exportFileName)
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
        writeMutex.withLock {
            withContext(backgroundDispatcher) {
                for (i in 0..currentFileIndex) {
                    try {
                        fileSystem.delete("logs_$i.jsonl")
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                currentFileIndex = 0
            }
        }
    }

    override suspend fun count(): Int {
        return countAtomic.value
    }

    override suspend fun prune(policy: com.spectra.logger.core.storage.RetentionPolicy): Int {
        if (!policy.hasLimits) return 0
        val now = com.spectra.logger.core.utils.SpectraTime.now().toEpochMilliseconds()
        var memoryPruned = 0

        // 1. Prune in-memory ring buffer
        synchronized(lock) {
            val maxAge = policy.maxAgeMs
            if (maxAge != null) {
                val cutoff = now - maxAge
                while (buffer.isNotEmpty() && buffer.first().timestamp.toEpochMilliseconds() < cutoff) {
                    buffer.removeFirst()
                    memoryPruned++
                }
            }

            val maxCount = policy.maxCount
            if (maxCount != null) {
                while (buffer.size > maxCount) {
                    buffer.removeFirst()
                    memoryPruned++
                }
            }
        }

        // 2. Prune disk files by maxSizeBytes
        var filePrunedLines = 0
        val maxSize = policy.maxSizeBytes
        if (maxSize != null) {
            writeMutex.withLock {
                withContext(backgroundDispatcher) {
                    try {
                        val files = fileSystem.listFiles(".").filter { it.startsWith("logs_") && it.endsWith(".jsonl") }
                        var totalSize = files.sumOf { fileSystem.getFileSize(it) }
                        val sortedFiles =
                            files.sortedBy { fileName ->
                                fileName.removePrefix("logs_").removeSuffix(".jsonl").toIntOrNull() ?: 0
                            }
                        for (file in sortedFiles) {
                            if (totalSize <= maxSize) break
                            val fileSize = fileSystem.getFileSize(file)
                            val lines = fileSystem.countLines(file)
                            fileSystem.delete(file)
                            totalSize -= fileSize
                            filePrunedLines += lines
                        }
                        countAtomic.addAndGet(-filePrunedLines)
                    } catch (_: Exception) {
                    }
                }
            }
        }

        return maxOf(memoryPruned, filePrunedLines)
    }

    private suspend fun rotateFiles() {
        currentFileIndex++
        val oldestFileIndex = currentFileIndex - maxFiles
        if (oldestFileIndex >= 0) {
            val fileName = "logs_$oldestFileIndex.jsonl"
            try {
                if (fileSystem.exists(fileName)) {
                    val deletedCount = fileSystem.countLines(fileName)
                    fileSystem.delete(fileName)
                    countAtomic.addAndGet(-deletedCount)
                }
            } catch (e: Exception) {
                // Ignore delete errors
            }
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

                for (fileName in logFiles) {
                    val idx = fileName.removePrefix("logs_").removeSuffix(".jsonl").toIntOrNull() ?: continue
                    if (idx < currentFileIndex - maxFiles + 1) {
                        try {
                            fileSystem.delete(fileName)
                        } catch (e: Exception) {
                        }
                    }
                }

                var total = 0
                for (i in currentFileIndex downTo maxOf(0, currentFileIndex - maxFiles + 1)) {
                    val fileName = "logs_$i.jsonl"
                    if (!fileSystem.exists(fileName)) continue
                    total += fileSystem.countLines(fileName)
                }
                countAtomic.value = total
            } catch (e: Exception) {
                println("Spectra File I/O Error: ${e.message}")
            }
        }
    }

    companion object {
        const val DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
        const val DEFAULT_MAX_FILES = 5
    }

    /**
     * Closes the storage, flushing buffers and cancelling all pending background operations.
     */
    suspend fun close() {
        flush()
        ioScope.coroutineContext[kotlinx.coroutines.Job]?.cancelAndJoin()
    }
}
