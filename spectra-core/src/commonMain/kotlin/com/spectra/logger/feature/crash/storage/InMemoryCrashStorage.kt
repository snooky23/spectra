package com.spectra.logger.feature.crash.storage

import com.spectra.logger.feature.crash.model.CrashReport
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryCrashStorage(
    private val maxCapacity: Int = DEFAULT_CAPACITY,
) : CrashStorage {
    private val lock = SynchronizedObject()
    private val buffer = ArrayDeque<CrashReport>(maxCapacity)
    private val crashesFlow = MutableStateFlow<List<CrashReport>>(emptyList())

    override suspend fun recordCrash(report: CrashReport) {
        recordCrashSync(report)
    }

    override fun recordCrashSync(report: CrashReport) {
        val updated =
            synchronized(lock) {
                if (buffer.size >= maxCapacity) {
                    buffer.removeFirst()
                }
                buffer.addLast(report)
                buffer.toList().reversed()
            }
        crashesFlow.value = updated
    }

    override suspend fun getCrashes(limit: Int?): List<CrashReport> =
        synchronized(lock) {
            val list = buffer.toList().reversed()
            if (limit != null && limit > 0) list.take(limit) else list
        }

    override suspend fun getLatestCrash(): CrashReport? =
        synchronized(lock) {
            buffer.lastOrNull()
        }

    override fun observeCrashes(): Flow<List<CrashReport>> = crashesFlow.asStateFlow()

    override suspend fun count(): Int =
        synchronized(lock) {
            buffer.size
        }

    override suspend fun clear() {
        synchronized(lock) {
            buffer.clear()
        }
        crashesFlow.value = emptyList()
    }

    companion object {
        const val DEFAULT_CAPACITY = 20
    }
}
