package com.spectra.logger.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerRow(
    label: String,
    timestamp: Instant?,
    onTimestampSelected: (Instant?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = timestamp?.toString() ?: "Not set",
            style = MaterialTheme.typography.bodyMedium,
            color = if (timestamp != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        )
    }

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    timestamp?.toEpochMilliseconds()
                        ?: com.spectra.logger.utils.SpectraTime.now().toEpochMilliseconds(),
            )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    if (selectedDateMillis != null) {
                        showTimePicker = true
                    }
                }) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val defaultTz = TimeZone.currentSystemDefault()
        val now = com.spectra.logger.utils.SpectraTime.now()
        val currentDateTime = (timestamp ?: now).toLocalDateTime(defaultTz)

        val timePickerState =
            rememberTimePickerState(
                initialHour = currentDateTime.hour,
                initialMinute = currentDateTime.minute,
                is24Hour = true,
            )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    selectedDateMillis?.let { dateMillis ->
                        val date =
                            Instant.fromEpochMilliseconds(dateMillis)
                                .toLocalDateTime(TimeZone.UTC).date

                        val localDateTime =
                            LocalDateTime(
                                year = date.year,
                                monthNumber = date.monthNumber,
                                dayOfMonth = date.dayOfMonth,
                                hour = timePickerState.hour,
                                minute = timePickerState.minute,
                                second = 0,
                                nanosecond = 0,
                            )

                        onTimestampSelected(localDateTime.toInstant(defaultTz))
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
