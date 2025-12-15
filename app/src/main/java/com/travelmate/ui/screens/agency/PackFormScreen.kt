package com.travelmate.ui.screens.agency

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.travelmate.data.models.CreatePackRequest
import com.travelmate.data.models.Pack
import com.travelmate.data.models.UpdatePackRequest
import com.travelmate.ui.theme.ColorPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Holds the mutable form values shared between create & edit screens. */
data class PackFormValues(
    val title: String = "",
    val destination: String = "",
    val country: String = "",
    val region: String = "",
    val description: String = "",
    val price: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val pensionType: String = "",
    val transportType: String = "",
    val hotelCategory: String = "",
    val activities: List<String> = emptyList(),
    val placesToVisit: List<String> = emptyList(),
    val images: List<String> = emptyList()
)

fun PackFormValues.toCreateRequest(): CreatePackRequest = CreatePackRequest(
    titre = title,
    description = description,
    prix = price.toDoubleOrNull() ?: 0.0,
    dateDebut = startDate,
    dateFin = endDate,
    destination = destination,
    images = images
)

fun PackFormValues.toUpdateRequest(): UpdatePackRequest = UpdatePackRequest(
    titre = title,
    description = description,
    prix = price.toDoubleOrNull(),
    dateDebut = startDate,
    dateFin = endDate,
    destination = destination,
    images = images
)

fun Pack.toFormValues(): PackFormValues = PackFormValues(
    title = titre,
    destination = destination.orEmpty(),
    description = description,
    price = prix.toString(),
    startDate = dateDebut,
    endDate = dateFin,
    images = images
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackFormScaffold(
    title: String,
    submitButtonLabel: String,
    initialValues: PackFormValues,
    onNavigateBack: () -> Unit,
    onSubmit: (PackFormValues) -> Unit,
    submitInProgress: Boolean = false,
    successDialog: SuccessDialogState = SuccessDialogState(),
    requireFullDetails: Boolean = true
) {
    var formValues by remember { mutableStateOf(initialValues) }
    var activities by remember { mutableStateOf(initialValues.activities) }
    var places by remember { mutableStateOf(initialValues.placesToVisit) }
    var images by remember { mutableStateOf(initialValues.images) }
    var activityInput by remember { mutableStateOf("") }
    var placeInput by remember { mutableStateOf("") }

    LaunchedEffect(initialValues) {
        formValues = initialValues
        activities = initialValues.activities
        places = initialValues.placesToVisit
        images = initialValues.images
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            images = (images + uris.map(Uri::toString)).distinct()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Ajouter des images")
            }
            if (images.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(images) { uriString ->
                        Box {
                            AsyncImage(
                                model = uriString,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { images = images - uriString },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(ColorPrimary, CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = formValues.title,
                onValueChange = { formValues = formValues.copy(title = it) },
                label = { Text("Titre") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formValues.destination,
                onValueChange = { formValues = formValues.copy(destination = it) },
                label = { Text("Destination (Ville / Lieu)") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            CountryRegionDropdown(
                country = formValues.country,
                onCountryChange = { formValues = formValues.copy(country = it) },
                region = formValues.region,
                onRegionChange = { formValues = formValues.copy(region = it) }
            )

            DateRangeFields(
                startDate = formValues.startDate,
                endDate = formValues.endDate,
                onStartDateChange = { formValues = formValues.copy(startDate = it) },
                onEndDateChange = { formValues = formValues.copy(endDate = it) }
            )

            PensionTransportSection(
                pensionType = formValues.pensionType,
                transportType = formValues.transportType,
                hotelCategory = formValues.hotelCategory,
                onPensionChange = { formValues = formValues.copy(pensionType = it) },
                onTransportChange = { formValues = formValues.copy(transportType = it) },
                onHotelChange = { formValues = formValues.copy(hotelCategory = it) }
            )

            TagInputField(
                label = "Activités",
                currentValue = activityInput,
                tags = activities,
                onValueChange = { activityInput = it },
                onAddTag = {
                    val cleaned = activityInput.trim()
                    if (cleaned.isNotEmpty()) {
                        activities = activities + cleaned
                        activityInput = ""
                    }
                },
                onRemoveTag = { tag -> activities = activities - tag }
            )

            TagInputField(
                label = "Lieux à visiter",
                currentValue = placeInput,
                tags = places,
                onValueChange = { placeInput = it },
                onAddTag = {
                    val cleaned = placeInput.trim()
                    if (cleaned.isNotEmpty()) {
                        places = places + cleaned
                        placeInput = ""
                    }
                },
                onRemoveTag = { tag -> places = places - tag }
            )

            Text("Prix du pack", style = MaterialTheme.typography.titleMedium, color = ColorPrimary)
            OutlinedTextField(
                value = formValues.price,
                onValueChange = { value ->
                    val filtered = value.filter { it.isDigit() || it == '.' }
                    formValues = formValues.copy(price = filtered)
                },
                label = { Text("Prix (DT)") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formValues.description,
                onValueChange = { formValues = formValues.copy(description = it) },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val current = formValues.copy(
                        activities = activities,
                        placesToVisit = places,
                        images = images
                    )
                    val validationError = validatePackForm(current, requireFullDetails)
                    if (validationError != null) {
                        coroutineScope.launch { snackbarHostState.showSnackbar(validationError) }
                    } else {
                        onSubmit(current)
                    }
                },
                enabled = !submitInProgress,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (submitInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(submitButtonLabel)
            }
        }
    }

    if (successDialog.visible) {
        AlertDialog(
            onDismissRequest = successDialog.onDismiss,
            title = successDialog.title?.let { { Text(it) } },
            text = successDialog.message?.let { { Text(it) } },
            confirmButton = {
                Button(onClick = successDialog.onConfirm ?: successDialog.onDismiss) {
                    Text(successDialog.confirmLabel)
                }
            },
            dismissButton = successDialog.dismissLabel?.let {
                {
                    OutlinedButton(onClick = successDialog.onDismiss) {
                        Text(it)
                    }
                }
            }
        )
    }
}

data class SuccessDialogState(
    val visible: Boolean = false,
    val title: String? = null,
    val message: String? = null,
    val confirmLabel: String = "OK",
    val dismissLabel: String? = null,
    val onConfirm: (() -> Unit)? = null,
    val onDismiss: () -> Unit = {}
)

@Composable
fun CountryRegionDropdown(
    country: String,
    onCountryChange: (String) -> Unit,
    region: String,
    onRegionChange: (String) -> Unit
) {
    val countries = listOf("Tunisie", "France", "Espagne", "Italie", "Allemagne")
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (country.isBlank()) "Sélectionner un pays" else country,
                color = if (country.isBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        countries.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    onCountryChange(option)
                    expanded = false
                }
            )
        }
    }

    if (country.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = region,
            onValueChange = onRegionChange,
            label = { Text("Région") },
            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DateRangeFields(
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    fun openDatePicker(current: String, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        if (current.isNotBlank()) {
            runCatching { dateFormatter.parse(current) }?.getOrNull()?.let { calendar.time = it }
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DatePickerField(
            label = "Date de début",
            value = startDate,
            modifier = Modifier.weight(1f),
            onClick = { openDatePicker(startDate, onStartDateChange) }
        )
        DatePickerField(
            label = "Date de fin",
            value = endDate,
            modifier = Modifier.weight(1f),
            onClick = { openDatePicker(endDate, onEndDateChange) }
        )
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            placeholder = { Text("Sélectionner") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun PensionTransportSection(
    pensionType: String,
    transportType: String,
    hotelCategory: String,
    onPensionChange: (String) -> Unit,
    onTransportChange: (String) -> Unit,
    onHotelChange: (String) -> Unit
) {
    val pensionOptions = listOf("Tout compris", "Demi-pension", "Petit-déjeuner inclus")
    val transportOptions = listOf("Aucun", "Transfert inclus", "Location de voiture")
    val hotelOptions = listOf("1 étoile", "2 étoiles", "3 étoiles", "4 étoiles", "5 étoiles")

    DropdownRow(
        label = if (pensionType.isBlank()) "Sélectionner un type de pension" else pensionType,
        currentValue = pensionType,
        options = pensionOptions,
        onOptionSelected = onPensionChange
    )
    Spacer(modifier = Modifier.height(8.dp))
    DropdownRow(
        label = if (transportType.isBlank()) "Sélectionner un type de transport" else transportType,
        currentValue = transportType,
        options = transportOptions,
        onOptionSelected = onTransportChange
    )
    Spacer(modifier = Modifier.height(8.dp))
    DropdownRow(
        label = if (hotelCategory.isBlank()) "Sélectionner une catégorie d'hôtel" else hotelCategory,
        currentValue = hotelCategory,
        options = hotelOptions,
        onOptionSelected = onHotelChange
    )
}

@Composable
private fun DropdownRow(
    label: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (currentValue.isBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    onOptionSelected(option)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun TagInputField(
    label: String,
    currentValue: String,
    tags: List<String>,
    onValueChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = currentValue,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = onAddTag) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tags) { tag ->
                AssistChip(
                    onClick = { onRemoveTag(tag) },
                    label = { Text(tag) },
                    leadingIcon = {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = ColorPrimary.copy(alpha = 0.08f))
                )
            }
        }
    }
}

private fun validatePackForm(
    values: PackFormValues,
    requireFullDetails: Boolean
): String? {
    if (values.title.isBlank()) return "Le titre est obligatoire"
    if (values.destination.isBlank()) return "La destination est obligatoire"
    if (values.startDate.isBlank() || values.endDate.isBlank()) return "Veuillez préciser les dates"
    if (values.price.toDoubleOrNull() == null || values.price.toDouble() <= 0) return "Prix invalide"
    if (requireFullDetails) {
        if (values.country.isBlank()) return "Veuillez sélectionner un pays"
        if (values.region.isBlank()) return "Veuillez renseigner la région"
        if (values.pensionType.isBlank()) return "Sélectionnez un type de pension"
        if (values.transportType.isBlank()) return "Sélectionnez un type de transport"
        if (values.hotelCategory.isBlank()) return "Sélectionnez une catégorie d'hôtel"
    }
    return null
}
