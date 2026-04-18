package com.spectra.logger.ui.compose.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spectra.logger.ui.compose.AppearanceMode
import com.spectra.logger.ui.theme.SpectraDesignTokens

/**
 * Spectra Logger theme wrapper that respects the appearance mode setting
 */
@Composable
fun SpectraTheme(
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (appearanceMode) {
            AppearanceMode.LIGHT -> false
            AppearanceMode.DARK -> true
            AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        }

    val colorScheme =
        if (darkTheme) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

/**
 * Search bar component matching iOS design
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = SpectraDesignTokens.SearchBarShape,
        colors =
            TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
    )
}

/**
 * Badge for active filters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFilterBadge(
    label: String,
    onRemove: () -> Unit,
) {
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(18.dp),
            )
        },
    )
}

/**
 * Empty state component
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Section container for settings
 */
@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

/**
 * Smart share bottom sheet with options for filtered or all logs
 * Matches iOS Action Sheet design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    filteredCount: Int,
    totalCount: Int,
    onShareFiltered: () -> Unit,
    onShareAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Share Logs",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Button(
                onClick = {
                    onShareFiltered()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Share Filtered ($filteredCount items)")
            }

            OutlinedButton(
                onClick = {
                    onShareAll()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Share All ($totalCount items)")
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel")
            }
        }
    }
}

/**
 * Badge for log levels
 */
@Composable
fun LogLevelBadge(level: com.spectra.logger.domain.model.LogLevel) {
    val color = colorForLogLevel(level)
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = SpectraDesignTokens.FilterChipAlpha),
    ) {
        Text(
            text = level.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

fun colorForLogLevel(level: com.spectra.logger.domain.model.LogLevel): Color {
    return when (level) {
        com.spectra.logger.domain.model.LogLevel.VERBOSE -> SpectraDesignTokens.VerboseGray
        com.spectra.logger.domain.model.LogLevel.DEBUG -> SpectraDesignTokens.DebugBlue
        com.spectra.logger.domain.model.LogLevel.INFO -> SpectraDesignTokens.InfoGreen
        com.spectra.logger.domain.model.LogLevel.WARNING -> SpectraDesignTokens.WarningOrange
        com.spectra.logger.domain.model.LogLevel.ERROR -> SpectraDesignTokens.ErrorRed
        com.spectra.logger.domain.model.LogLevel.FATAL -> SpectraDesignTokens.FatalPurple
    }
}

/**
 * Standard detail section for panes and sheets
 */
@Composable
fun DetailSection(
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = titleColor,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

/**
 * Status badge for network requests
 */
@Composable
fun StatusBadge(
    code: Int?,
    error: String?,
) {
    val color = colorForStatusRange(code, error)

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = SpectraDesignTokens.FilterChipAlpha),
    ) {
        Text(
            text = code?.toString() ?: "ERR",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

fun colorForStatusRange(
    code: Int?,
    error: String?,
): Color {
    return when {
        error != null || code == null -> SpectraDesignTokens.ErrorRed
        code in 200..299 -> SpectraDesignTokens.InfoGreen
        code in 300..399 -> SpectraDesignTokens.DebugBlue
        code in 400..499 -> SpectraDesignTokens.WarningOrange
        else -> SpectraDesignTokens.ErrorRed
    }
}

fun colorForStatusRange(range: String): Color {
    return when (range) {
        "2xx" -> SpectraDesignTokens.InfoGreen
        "3xx" -> SpectraDesignTokens.DebugBlue
        "4xx" -> SpectraDesignTokens.WarningOrange
        "5xx" -> SpectraDesignTokens.ErrorRed
        else -> SpectraDesignTokens.VerboseGray
    }
}
