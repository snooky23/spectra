package com.spectra.logger.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.spectra.logger.SpectraLogger
import com.spectra.logger.domain.model.LogFilter
import com.spectra.logger.domain.model.NetworkLogFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android file export and sharing utilities.
 */
object FileExporter {
    /**
     * Export logs to a file and return the file.
     *
     * @param context The context
     * @param format Export format
     * @param filter Optional log filter
     * @param fileName Optional custom filename
     * @return The exported file
     */
    suspend fun exportLogsToFile(
        context: Context,
        format: ExportFormat = ExportFormat.TEXT,
        filter: LogFilter = LogFilter.NONE,
        fileName: String? = null,
    ): File =
        withContext(Dispatchers.IO) {
            val extension =
                when (format) {
                    ExportFormat.TEXT -> "txt"
                    ExportFormat.JSON -> "json"
                    ExportFormat.CSV -> "csv"
                }

            val defaultFileName = "spectra_logs_${System.currentTimeMillis()}.$extension"
            val file = File(context.cacheDir, fileName ?: defaultFileName)

            val content =
                when (format) {
                    ExportFormat.TEXT -> LogExporter.exportLogsAsText(SpectraLogger.logStorage, filter)
                    ExportFormat.JSON -> LogExporter.exportLogsAsJson(SpectraLogger.logStorage, filter)
                    ExportFormat.CSV -> LogExporter.exportLogsAsCsv(SpectraLogger.logStorage, filter)
                }

            file.writeText(content)
            file
        }

    /**
     * Export network logs to a file and return the file.
     *
     * @param context The context
     * @param format Export format
     * @param filter Optional network log filter
     * @param fileName Optional custom filename
     * @return The exported file
     */
    suspend fun exportNetworkLogsToFile(
        context: Context,
        format: ExportFormat = ExportFormat.TEXT,
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
        fileName: String? = null,
    ): File =
        withContext(Dispatchers.IO) {
            val extension =
                when (format) {
                    ExportFormat.TEXT -> "txt"
                    ExportFormat.JSON -> "json"
                    ExportFormat.CSV -> "csv"
                }

            val defaultFileName = "spectra_network_logs_${System.currentTimeMillis()}.$extension"
            val file = File(context.cacheDir, fileName ?: defaultFileName)

            val content =
                when (format) {
                    ExportFormat.TEXT ->
                        LogExporter.exportNetworkLogsAsText(
                            SpectraLogger.networkStorage,
                            filter,
                        )

                    ExportFormat.JSON ->
                        LogExporter.exportNetworkLogsAsJson(
                            SpectraLogger.networkStorage,
                            filter,
                        )

                    ExportFormat.CSV -> "" // CSV not implemented for network logs
                }

            file.writeText(content)
            file
        }

    /**
     * Share logs via Android share sheet.
     *
     * @param context The context
     * @param format Export format
     * @param filter Optional log filter
     */
    suspend fun shareLogs(
        context: Context,
        format: ExportFormat = ExportFormat.TEXT,
        filter: LogFilter = LogFilter.NONE,
    ) {
        val file = exportLogsToFile(context, format, filter)
        shareFile(context, file, "application/octet-stream")
    }

    /**
     * Share network logs via Android share sheet.
     *
     * @param context The context
     * @param format Export format
     * @param filter Optional network log filter
     */
    suspend fun shareNetworkLogs(
        context: Context,
        format: ExportFormat = ExportFormat.TEXT,
        filter: NetworkLogFilter = NetworkLogFilter.NONE,
    ) {
        val file = exportNetworkLogsToFile(context, format, filter)
        shareFile(context, file, "application/octet-stream")
    }

    /**
     * Share a file using Android share sheet.
     *
     * @param context The context
     * @param file The file to share
     * @param mimeType MIME type of the file
     */
    private fun shareFile(
        context: Context,
        file: File,
        mimeType: String,
    ) {
        val uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.spectra.fileprovider",
                file,
            )

        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

        val chooser = Intent.createChooser(shareIntent, "Share Spectra Logs")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
