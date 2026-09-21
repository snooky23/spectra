package com.spectra.logger.feature.crash.storage

import com.spectra.logger.core.storage.FileSystem
import com.spectra.logger.feature.crash.model.CrashReport
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

class FileCrashStorage(
    private val fileSystem: FileSystem,
    private val maxCrashes: Int = DEFAULT_MAX_CRASHES,
    private val subDirectory: String = DEFAULT_SUBDIRECTORY,
) : CrashStorage {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val lock = SynchronizedObject()
    private val cachedCrashes = mutableListOf<CrashReport>()
    private val crashesFlow = MutableStateFlow<List<CrashReport>>(emptyList())
    private var isLoaded = false

    init {
        loadExistingCrashes()
    }

    private fun loadExistingCrashes() {
        val fs = fileSystem.okioFs ?: return
        val dirPath = fileSystem.getAbsolutePath(subDirectory).toPath()
        synchronized(lock) {
            try {
                if (fs.exists(dirPath)) {
                    val files =
                        fs.list(dirPath)
                            .filter { it.name.startsWith("crash_") && it.name.endsWith(".json") }
                            .sortedByDescending { it.name }

                    val loaded = mutableListOf<CrashReport>()
                    for (file in files) {
                        try {
                            val content = fs.source(file).buffer().use { it.readUtf8() }
                            val report = json.decodeFromString<CrashReport>(content)
                            loaded.add(report)
                        } catch (_: Exception) {
                            // Skip corrupted file
                        }
                    }
                    cachedCrashes.clear()
                    cachedCrashes.addAll(loaded.take(maxCrashes))
                    crashesFlow.value = cachedCrashes.toList()
                }
            } catch (_: Exception) {
                // Ignore I/O read failure during startup
            }
            isLoaded = true
        }
    }

    override suspend fun recordCrash(report: CrashReport) {
        recordCrashSync(report)
    }

    override fun recordCrashSync(report: CrashReport) {
        val fs = fileSystem.okioFs
        val fullPath = fileSystem.getAbsolutePath("$subDirectory/crash_${report.timestamp}_${report.id}.json").toPath()

        synchronized(lock) {
            try {
                if (fs != null) {
                    val parent = fullPath.parent
                    if (parent != null && !fs.exists(parent)) {
                        fs.createDirectories(parent)
                    }
                    fs.sink(fullPath).buffer().use { sink ->
                        sink.writeUtf8(json.encodeToString(report))
                    }
                }
            } catch (_: Exception) {
                // Best-effort synchronous write before crash
            }

            cachedCrashes.add(0, report)
            if (cachedCrashes.size > maxCrashes) {
                val excess = cachedCrashes.removeAt(cachedCrashes.size - 1)
                if (fs != null) {
                    try {
                        val excessPath = fileSystem.getAbsolutePath("$subDirectory/crash_${excess.timestamp}_${excess.id}.json").toPath()
                        if (fs.exists(excessPath)) {
                            fs.delete(excessPath)
                        }
                    } catch (_: Exception) {
                        // Ignore deletion error
                    }
                }
            }
            crashesFlow.value = cachedCrashes.toList()
        }
    }

    override suspend fun getCrashes(limit: Int?): List<CrashReport> =
        synchronized(lock) {
            if (!isLoaded) loadExistingCrashes()
            if (limit != null && limit > 0) cachedCrashes.take(limit) else cachedCrashes.toList()
        }

    override suspend fun getLatestCrash(): CrashReport? =
        synchronized(lock) {
            if (!isLoaded) loadExistingCrashes()
            cachedCrashes.firstOrNull()
        }

    override fun observeCrashes(): Flow<List<CrashReport>> = crashesFlow.asStateFlow()

    override suspend fun count(): Int =
        synchronized(lock) {
            if (!isLoaded) loadExistingCrashes()
            cachedCrashes.size
        }

    override suspend fun clear() {
        val fs = fileSystem.okioFs
        val dirPath = fileSystem.getAbsolutePath(subDirectory).toPath()
        synchronized(lock) {
            if (fs != null && fs.exists(dirPath)) {
                try {
                    fs.list(dirPath).forEach { file ->
                        fs.delete(file)
                    }
                } catch (_: Exception) {
                    // Ignore deletion error
                }
            }
            cachedCrashes.clear()
            crashesFlow.value = emptyList()
        }
    }

    companion object {
        const val DEFAULT_MAX_CRASHES = 20
        const val DEFAULT_SUBDIRECTORY = "crashes"
    }
}
