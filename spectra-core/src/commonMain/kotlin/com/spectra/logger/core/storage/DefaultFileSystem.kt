package com.spectra.logger.core.storage

import okio.FileSystem

/**
 * Platform-specific default FileSystem for Okio.
 * On platforms that support file system access natively (JVM, Native), this returns [FileSystem.SYSTEM].
 * On Web platforms (JS, Wasm), this returns null as there is no universal synchronous file system.
 */
expect val defaultFileSystem: FileSystem?
