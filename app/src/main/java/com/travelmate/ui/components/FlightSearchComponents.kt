package com.travelmate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.travelmate.data.models.Airport
import com.travelmate.data.models.PopularAirports
import com.travelmate.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable pour l'autocomplete d'aéroports avec recherche en temps réel
 * Permet la saisie continue sans interruption
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirportAutocomplete(
    label: String,
    selectedAirport: Airport?,
    onAirportSelected: (Airport) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<Airport>>(emptyList()) }
    
    // Mettre à jour la query quand l'aéroport sélectionné change (seulement si vide)
    LaunchedEffect(selectedAirport) {
        if (selectedAirport != null && selectedAirport.code.isNotEmpty() && query.isEmpty()) {
            query = "${selectedAirport.name} (${selectedAirport.code})"
        } else if (selectedAirport == null || selectedAirport.code.isEmpty()) {
            if (query.isEmpty()) {
                // Ne pas effacer si l'utilisateur est en train de taper
            }
        }
    }
    
    // Recherche d'aéroports en temps réel (déclenché par le changement de query)
    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            suggestions = PopularAirports.search(query)
            // Ne pas forcer l'expansion - laisser l'utilisateur continuer à taper
            if (suggestions.isNotEmpty()) {
                expanded = true
            } else {
                expanded = false
            }
        } else {
            suggestions = emptyList()
            expanded = false
        }
    }
    
    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded && suggestions.isNotEmpty() && query.isNotEmpty(),
            onExpandedChange = { 
                expanded = it
            }
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { newValue ->
                    query = newValue
                    // Si l'utilisateur efface tout, désélectionner l'aéroport
                    if (newValue.isEmpty()) {
                        onAirportSelected(Airport())
                        expanded = false
                    }
                    // Laisser LaunchedEffect gérer l'expansion automatiquement
                },
                label = { Text(label, maxLines = 1) },
                placeholder = { Text("Ex: Tunis, TUN, Tunisia", maxLines = 1) },
                leadingIcon = {
                    Icon(
                        if (label.contains("partez")) Icons.Default.FlightTakeoff else Icons.Default.FlightLand,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            onAirportSelected(Airport())
                            expanded = false
                        }) {
                            Icon(Icons.Default.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                readOnly = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                singleLine = true,
                maxLines = 1,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                ),
                enabled = enabled
            )
            
            ExposedDropdownMenu(
                expanded = expanded && suggestions.isNotEmpty() && query.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                if (suggestions.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Aucun aéroport trouvé") },
                        onClick = { expanded = false }
                    )
                } else {
                    suggestions.forEach { airport ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "${airport.name} (${airport.code})",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    if (airport.city != null || airport.country != null) {
                                        Text(
                                            text = "${airport.city ?: ""}${if (airport.city != null && airport.country != null) ", " else ""}${airport.country ?: ""}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                query = "${airport.name} (${airport.code})"
                                onAirportSelected(airport)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable pour le sélecteur de date user-friendly
 */
@Composable
fun DatePickerField(
    label: String,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minDate: Long? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale("fr", "FR"))
    }
    
    val apiDateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
    
    val displayText = remember(selectedDate) {
        if (selectedDate.isNullOrEmpty()) {
            ""
        } else {
            try {
                val date = apiDateFormatter.parse(selectedDate)
                date?.let { dateFormatter.format(it) } ?: selectedDate
            } catch (e: Exception) {
                selectedDate
            }
        }
    }
    
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(label, maxLines = 1) },
            placeholder = { Text("Sélectionner une date", maxLines = 1) },
            leadingIcon = {
                Icon(
                    Icons.Default.CalendarToday,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            enabled = false // Disable to prevent keyboard
        )
        
        // Invisible clickable box on top to catch mouse clicks
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { 
                    if (enabled) {
                        showDatePicker = true
                    }
                }
        )
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.let {
                try {
                    apiDateFormatter.parse(it)?.time
                } catch (e: Exception) {
                    null
                }
            },
            yearRange = IntRange(2024, 2030)
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onConfirm = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Date(millis)
                    val formattedDate = apiDateFormatter.format(date)
                    onDateSelected(formattedDate)
                }
                showDatePicker = false
            },
            datePickerState = datePickerState,
            minDate = minDate
        )
    }
}

/**
 * Dialog pour le DatePicker Material3
 */
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    datePickerState: DatePickerState,
    minDate: Long? = null
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Annuler", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Confirmer", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

/**
 * Composable pour le sélecteur de nombre de passagers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdultsSelector(
    adults: Int,
    onAdultsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = adults.toString(),
            onValueChange = {},
            label = { Text("Nombre de passagers") },
            placeholder = { Text("1") },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            readOnly = true,
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            ),
            enabled = enabled
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..9).forEach { count ->
                DropdownMenuItem(
                    text = { Text("$count ${if (count == 1) "passager" else "passagers"}") },
                    onClick = {
                        onAdultsChanged(count)
                        expanded = false
                    }
                )
            }
        }
    }
}

