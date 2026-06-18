package com.humblecoders.matricareog.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.humblecoders.matricareog.util.PregnancyUtils

private val PrimaryPink = Color(0xFFE91E63)
private val TextMuted = Color(0xFF666666)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDatePickerDialog(
    currentDueDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (dueDateMillis: Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = PregnancyUtils.parseDueDate(currentDueDate)
            ?: PregnancyUtils.defaultDueDatePickerMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                onDismiss()
            }) {
                Text("OK", color = PrimaryPink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
