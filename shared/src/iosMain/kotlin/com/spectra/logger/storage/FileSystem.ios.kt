package com.spectra.logger.storage

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringByDeletingLastPathComponent
import platform.Foundation.writeToFile

/**
 * iOS implementation of FileSystem using app's Documents directory.
 *
 * Files are stored in NSDocumentDirectory which is backed up by iCloud.
 */
@OptIn(ExperimentalForeignApi::class)
actual class FileSystem {
    private val fileManager = NSFileManager.defaultManager
    private val baseDir: String by lazy {
        val paths =
            NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true,
            )
        val documentsDirectory = paths.first() as String
        "$documentsDirectory/spectra_logs"
    }

    init {
        // Create base directory if it doesn't exist
        if (!fileManager.fileExistsAtPath(baseDir)) {
            fileManager.createDirectoryAtPath(
                baseDir,
                true,
                null,
                null,
            )
        }
    }

    private fun getFilePath(path: String): String = "$baseDir/$path"

    actual suspend fun writeText(
        path: String,
        content: String,
        append: Boolean,
    ): Unit =
        withContext(Dispatchers.Default) {
            val filePath = getFilePath(path)

            // Create parent directories if needed
            val nsFilePath = filePath as NSString
            val parentPath = nsFilePath.stringByDeletingLastPathComponent()
            if (!fileManager.fileExistsAtPath(parentPath)) {
                fileManager.createDirectoryAtPath(parentPath, true, null, null)
            }

            if (append && fileManager.fileExistsAtPath(filePath)) {
                // Append to existing file - read existing content, append, write
                val existingContent = readText(path) ?: ""
                val newContent = existingContent + content
                val nsContent = NSString.create(string = newContent)
                nsContent.writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
            } else {
                // Write new file
                val nsContent = NSString.create(string = content)
                nsContent.writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
            }
        }

    actual suspend fun readText(path: String): String? =
        withContext(Dispatchers.Default) {
            val filePath = getFilePath(path)
            if (fileManager.fileExistsAtPath(filePath)) {
                val data = NSData.create(contentsOfFile = filePath)
                data?.let {
                    NSString.create(data = it, encoding = NSUTF8StringEncoding) as? String
                }
            } else {
                null
            }
        }

    actual suspend fun exists(path: String): Boolean =
        withContext(Dispatchers.Default) {
            fileManager.fileExistsAtPath(getFilePath(path))
        }

    actual suspend fun delete(path: String): Boolean =
        withContext(Dispatchers.Default) {
            val filePath = getFilePath(path)
            if (fileManager.fileExistsAtPath(filePath)) {
                fileManager.removeItemAtPath(filePath, null)
            } else {
                false
            }
        }

    actual suspend fun getFileSize(path: String): Long =
        withContext(Dispatchers.Default) {
            val filePath = getFilePath(path)
            if (fileManager.fileExistsAtPath(filePath)) {
                val attributes = fileManager.attributesOfItemAtPath(filePath, null)
                (attributes?.get(platform.Foundation.NSFileSize) as? Long) ?: 0L
            } else {
                0L
            }
        }

    actual suspend fun listFiles(path: String): List<String> =
        withContext(Dispatchers.Default) {
            val dirPath = getFilePath(path)
            if (fileManager.fileExistsAtPath(dirPath)) {
                val contents = fileManager.contentsOfDirectoryAtPath(dirPath, null)
                contents?.map { it.toString() } ?: emptyList()
            } else {
                emptyList()
            }
        }
}
