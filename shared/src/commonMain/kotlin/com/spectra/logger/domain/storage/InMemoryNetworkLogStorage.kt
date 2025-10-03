package com.spectra.logger.domain.storage

import com.spectra.logger.domain.model.NetworkLogEntry
import com.spectra.logger.domain.model.NetworkLogFilter
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

/**
 * In-memory network log storage implementation using a circular buffer.
 * Thread-safe with optimized read/write performance.
 *
 * @property maxCapacity Maximum number of network log entries to store (default 1,000)
 */
class InMemoryNetworkLogStorage(
    private val maxCapacity: Int = DEFAULT_CAPACITY,
) : NetworkLogStorage {
    private val lock = SynchronizedObject()
    private val buffer = ArrayDeque<NetworkLogEntry>(maxCapacity)
    private val logFlow =
        MutableSharedFlow<NetworkLogEntry>(
            replay = 0,
            extraBufferCapacity = FLOW_BUFFER_CAPACITY,
        )
    private val countAtomic = atomic(0)

    override suspend fun add(entry: NetworkLogEntry) {
        synchronized(lock) {
            if (buffer.size >= maxCapacity) {
                buffer.removeFirst()
            } else {
                countAtomic.incrementAndGet()
            }
            buffer.addLast(entry)
        }
        logFlow.emit(entry)
    }

    override suspend fun query(
        filter: NetworkLogFilter,
        limit: Int?,
    ): List<NetworkLogEntry> =
        synchronized(lock) {
            buffer.asReversed()
                .asSequence()
                .filter { filter.matches(it) }
                .let { sequence ->
                    if (limit != null) sequence.take(limit) else sequence
                }
                .toList()
        }

    override fun observe(filter: NetworkLogFilter): Flow<NetworkLogEntry> = logFlow.filter { filter.matches(it) }

    override suspend fun count(): Int = countAtomic.value

    override suspend fun clear() {
        synchronized(lock) {
            buffer.clear()
            countAtomic.value = 0
        }
    }

    companion object {
        const val DEFAULT_CAPACITY = 1_000
        private const val FLOW_BUFFER_CAPACITY = 64
    }
}
