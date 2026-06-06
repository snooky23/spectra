package com.spectra.logger.feature.logs.storage

import com.spectra.logger.core.model.*
import com.spectra.logger.core.utils.*
import com.spectra.logger.feature.logs.model.LogEntry
import com.spectra.logger.feature.logs.model.LogFilter
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

/**
 * In-memory log storage implementation using a circular buffer.
 * Thread-safe with optimized read/write performance.
 *
 * @property maxCapacity Maximum number of log entries to store (default 10,000)
 */
class InMemoryLogStorage(
    maxCapacity: Int = DEFAULT_CAPACITY,
) : LogStorage {
    private val lock = SynchronizedObject()
    private var currentMaxCapacity = maxCapacity
    private val buffer = ArrayDeque<LogEntry>(maxCapacity)
    private val logFlow =
        MutableSharedFlow<LogEntry>(
            replay = 0,
            extraBufferCapacity = FLOW_BUFFER_CAPACITY,
        )
    private val countAtomic = atomic(0)

    fun updateCapacity(newCapacity: Int) {
        synchronized(lock) {
            currentMaxCapacity = newCapacity
            while (buffer.size > currentMaxCapacity) {
                buffer.removeFirst()
            }
            countAtomic.value = buffer.size
        }
    }

    override suspend fun add(entry: LogEntry) {
        synchronized(lock) {
            if (buffer.size >= currentMaxCapacity) {
                buffer.removeFirst()
            } else {
                countAtomic.incrementAndGet()
            }
            buffer.addLast(entry)
        }
        logFlow.emit(entry)
    }

    override suspend fun addAll(entries: List<LogEntry>) {
        if (entries.isEmpty()) return

        synchronized(lock) {
            entries.forEach { entry ->
                if (buffer.size >= currentMaxCapacity) {
                    buffer.removeFirst()
                } else {
                    countAtomic.incrementAndGet()
                }
                buffer.addLast(entry)
            }
        }

        // Emit all entries to flow
        entries.forEach { logFlow.emit(it) }
    }

    override suspend fun query(
        filter: LogFilter,
        limit: Int?,
    ): List<LogEntry> =
        synchronized(lock) {
            buffer.asReversed()
                .asSequence()
                .filter { filter.matches(it) }
                .let { sequence ->
                    if (limit != null) sequence.take(limit) else sequence
                }
                .toList()
        }

    override fun observe(filter: LogFilter): Flow<LogEntry> = logFlow.filter { filter.matches(it) }

    override suspend fun exportLogs(): String? {
        // InMemoryLogStorage does not have a FileSystem context to write an export file to.
        // To use export functionality, configure the logger with a FileLogStorage.
        println("SpectraLogger: Cannot export logs from InMemoryLogStorage because no directory path is configured.")
        return null
    }

    override suspend fun count(): Int = countAtomic.value

    override suspend fun clear() {
        synchronized(lock) {
            buffer.clear()
            countAtomic.value = 0
        }
    }

    companion object {
        const val DEFAULT_CAPACITY = 10_000
        private const val FLOW_BUFFER_CAPACITY = 64
    }
}
