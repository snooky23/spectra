package com.spectra.logger.storage

/**
 * Cross-platform file system abstraction for log persistence.
 *
 * This expect/actual pattern provides platform-specific file operations
 * while maintaining a common interface.
 */
expect class FileSystem {
    /**
     * Write text content to a file.
     *
     * @param path File path relative to app storage directory
     * @param content Text content to write
     * @param append If true, append to existing file; if false, overwrite
     */
    suspend fun writeText(
        path: String,
        content: String,
        append: Boolean = false,
    )

    /**
     * Read text content from a file.
     *
     * @param path File path relative to app storage directory
     * @return File content as string, or null if file doesn't exist
     */
    suspend fun readText(path: String): String?

    /**
     * Check if a file exists.
     *
     * @param path File path relative to app storage directory
     * @return True if file exists, false otherwise
     */
    suspend fun exists(path: String): Boolean

    /**
     * Delete a file.
     *
     * @param path File path relative to app storage directory
     * @return True if file was deleted, false if it didn't exist
     */
    suspend fun delete(path: String): Boolean

    /**
     * Get the size of a file in bytes.
     *
     * @param path File path relative to app storage directory
     * @return File size in bytes, or 0 if file doesn't exist
     */
    suspend fun getFileSize(path: String): Long

    /**
     * List all files in a directory.
     *
     * @param path Directory path relative to app storage directory
     * @return List of file names in the directory
     */
    suspend fun listFiles(path: String): List<String>
}
