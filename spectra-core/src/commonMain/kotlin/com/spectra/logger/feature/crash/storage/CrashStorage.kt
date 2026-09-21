package com.spectra.logger.feature.crash.storage

import com.spectra.logger.feature.crash.model.CrashReport
import kotlinx.coroutines.flow.Flow

interface CrashStorage {
    suspend fun recordCrash(report: CrashReport)

    fun recordCrashSync(report: CrashReport)

    suspend fun getCrashes(limit: Int? = null): List<CrashReport>

    suspend fun getLatestCrash(): CrashReport?

    fun observeCrashes(): Flow<List<CrashReport>>

    suspend fun count(): Int

    suspend fun clear()
}
