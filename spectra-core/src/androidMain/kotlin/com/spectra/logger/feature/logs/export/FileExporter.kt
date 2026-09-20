package com.spectra.logger.feature.logs.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.spectra.logger.SpectraLogger
import com.spectra.logger.core.utils.SpectraTime
import com.spectra.logger.feature.events.model.EventFilter
import com.spectra.logger.feature.logs.model.LogFilter
import com.spectra.logger.feature.network.model.NetworkLogFilter
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
                    ExportFormat.HAR -> "har"
                    ExportFormat.MARKDOWN -> "md"
                }

            val defaultFileName = "spectra_logs_${SpectraTime.now().toEpochMilliseconds()}.$extension"
            val file = File(context.cacheDir, fileName ?: defaultFileName)

            val content =
                when (format) {
                    ExportFormat.TEXT -> LogExporter.exportLogsAsText(SpectraLogger.logStorage, filter)
                    ExportFormat.JSON -> LogExporter.exportLogsAsJson(SpectraLogger.logStorage, filter)
                    ExportFormat.CSV -> LogExporter.exportLogsAsCsv(SpectraLogger.logStorage, filter)
                    ExportFormat.HAR -> LogExporter.exportLogsAsJson(SpectraLogger.logStorage, filter)
                    ExportFormat.MARKDOWN -> LogExporter.exportLogsAsMarkdown(SpectraLogger.logStorage, filter)
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
                    ExportFormat.HAR -> "har"
                    ExportFormat.MARKDOWN -> "md"
                }

            val defaultFileName = "spectra_network_logs_${SpectraTime.now().toEpochMilliseconds()}.$extension"
            val file = File(context.cacheDir, fileName ?: defaultFileName)

            val content =
                when (format) {
                    ExportFormat.TEXT -> LogExporter.exportNetworkLogsAsText(SpectraLogger.networkStorage, filter)
                    ExportFormat.JSON -> LogExporter.exportNetworkLogsAsJson(SpectraLogger.networkStorage, filter)
                    ExportFormat.CSV -> ""
                    ExportFormat.HAR -> LogExporter.exportNetworkLogsAsHar(SpectraLogger.networkStorage, filter)
                    ExportFormat.MARKDOWN -> LogExporter.exportNetworkLogsAsText(SpectraLogger.networkStorage, filter)
                }

            file.writeText(content)
            file
        }

    /**
     * Export events to a file and return the file.
     */
    suspend fun exportEventsToFile(
        context: Context,
        format: ExportFormat = ExportFormat.TEXT,
        filter: EventFilter = EventFilter.NONE,
        fileName: String? = null,
    ): File =
        withContext(Dispatchers.IO) {
            val extension =
                when (format) {
                    ExportFormat.TEXT -> "txt"
                    ExportFormat.JSON -> "json"
                    ExportFormat.CSV -> "csv"
                    ExportFormat.HAR -> "json"
                    ExportFormat.MARKDOWN -> "md"
                }

            val defaultFileName = "spectra_events_${SpectraTime.now().toEpochMilliseconds()}.$extension"
            val file = File(context.cacheDir, fileName ?: defaultFileName)

            val content =
                when (format) {
                    ExportFormat.TEXT -> LogExporter.exportEventsAsText(SpectraLogger.eventStorage, filter)
                    ExportFormat.JSON -> LogExporter.exportEventsAsJson(SpectraLogger.eventStorage, filter)
                    ExportFormat.CSV -> LogExporter.exportEventsAsCsv(SpectraLogger.eventStorage, filter)
                    ExportFormat.HAR -> LogExporter.exportEventsAsJson(SpectraLogger.eventStorage, filter)
                    ExportFormat.MARKDOWN -> LogExporter.exportEventsAsMarkdown(SpectraLogger.eventStorage, filter)
                }

            file.writeText(content)
            file
        }

    /**
     * Share logs via Android share sheet.
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
     * Share events via Android share sheet.
     */
    suspend fun shareEvents(
        context: Context,
        format: ExportFormat = ExportFormat.TEXT,
        filter: EventFilter = EventFilter.NONE,
    ) {
        val file = exportEventsToFile(context, format, filter)
        shareFile(context, file, "application/octet-stream")
    }

    /**
     * Share a file using Android share sheet.
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

        val chooser = Intent.createChooser(shareIntent, "Share Spectra Telemetry")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
