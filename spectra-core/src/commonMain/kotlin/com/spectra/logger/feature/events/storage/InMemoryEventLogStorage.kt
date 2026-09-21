package com.spectra.logger.feature.events.storage

import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.events.model.EventLogEntry
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

/**
 * Thread-safe in-memory ring-buffer storage for event logs.
 *
 * @property maxCapacity Maximum number of events to retain in memory (default 5,000)
 */
class InMemoryEventLogStorage(
    maxCapacity: Int = DEFAULT_CAPACITY,
) : EventLogStorage {
    private val lock = SynchronizedObject()
    private var currentMaxCapacity = maxCapacity
    private val buffer = ArrayDeque<EventLogEntry>(maxCapacity)
    private val eventFlow =
        MutableSharedFlow<EventLogEntry>(
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

    override suspend fun add(entry: EventLogEntry) {
        synchronized(lock) {
            if (buffer.size >= currentMaxCapacity) {
                buffer.removeFirst()
            } else {
                countAtomic.incrementAndGet()
            }
            buffer.addLast(entry)
        }
        eventFlow.emit(entry)
    }

    override suspend fun addAll(entries: List<EventLogEntry>) {
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

        entries.forEach { eventFlow.emit(it) }
    }

    override suspend fun query(
        filter: EventFilter,
        limit: Int?,
    ): List<EventLogEntry> =
        synchronized(lock) {
            buffer.asReversed()
                .asSequence()
                .filter { filter.matches(it) }
                .let { sequence ->
                    if (limit != null) sequence.take(limit) else sequence
                }
                .toList()
        }

    override fun observe(filter: EventFilter): Flow<EventLogEntry> = eventFlow.filter { filter.matches(it) }

    override suspend fun count(): Int = countAtomic.value

    override suspend fun clear() {
        synchronized(lock) {
            buffer.clear()
            countAtomic.value = 0
        }
    }

    override suspend fun prune(policy: com.spectra.logger.core.storage.RetentionPolicy): Int {
        if (!policy.hasLimits) return 0
        val now = com.spectra.logger.core.utils.SpectraTime.now().toEpochMilliseconds()
        var prunedCount = 0

        synchronized(lock) {
            if (buffer.isEmpty()) return 0

            // 1. Prune by maxAgeMs (TTL)
            val maxAge = policy.maxAgeMs
            if (maxAge != null) {
                val cutoff = now - maxAge
                while (buffer.isNotEmpty() && buffer.first().timestamp.toEpochMilliseconds() < cutoff) {
                    buffer.removeFirst()
                    prunedCount++
                }
            }

            // 2. Prune by maxCount (FIFO)
            val maxCount = policy.maxCount
            if (maxCount != null) {
                while (buffer.size > maxCount) {
                    buffer.removeFirst()
                    prunedCount++
                }
            }

            countAtomic.value = buffer.size
        }
        return prunedCount
    }

    companion object {
        const val DEFAULT_CAPACITY = 5_000
        private const val FLOW_BUFFER_CAPACITY = 64
    }
}
