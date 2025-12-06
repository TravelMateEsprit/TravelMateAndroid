package com.travelmate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.CalendarToday,
    placeholder: String = "Sélectionner une date",
    isError: Boolean = false,
    minDate: Long? = null,
    maxDate: Long? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Validation des dates sélectionnables
    val isDateValid: (Long) -> Boolean = { dateMillis ->
        when {
            minDate != null && dateMillis < minDate -> false
            maxDate != null && dateMillis > maxDate -> false
            else -> true
        }
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        placeholder = { Text(placeholder) },
        readOnly = true,
        isError = isError,
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        singleLine = true,
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = if (isError) MaterialTheme.colorScheme.error 
                                else MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = if (isError) MaterialTheme.colorScheme.error 
                                else MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
    
    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (value.isNotBlank()) {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value)?.time
                } catch (e: Exception) {
                    null
                }
            } else null
        )
        
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            onDateSelected(formatter.format(date))
                        }
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
