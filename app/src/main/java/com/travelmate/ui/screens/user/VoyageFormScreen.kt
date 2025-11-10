package com.travelmate.ui.screens.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travelmate.data.models.Voyage
import com.travelmate.ui.components.CustomTextField
import com.travelmate.ui.theme.*
import com.travelmate.utils.Constants
import com.travelmate.viewmodel.VoyagesViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
// Removed java.time imports as we're using SimpleDateFormat instead

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyageFormScreen(
    voyageId: String? = null, // null for create, non-null for edit
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: VoyagesViewModel = hiltViewModel()
) {
    val voyages by viewModel.voyages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val isEditMode = voyageId != null
    val existingVoyage = remember(voyageId, voyages) {
        voyageId?.let { id -> voyages.find { it.id_voyage == id } }
    }
    
    // Helper function to validate date format (compatible with API 24+)
    fun isValidDateFormat(date: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Date formatters
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    // Form state
    var destination by remember { mutableStateOf(existingVoyage?.destination ?: "") }
    var dateDepart by remember { 
        mutableStateOf(
            existingVoyage?.date_depart?.let { dateFormat.format(it) } ?: ""
        ) 
    }
    var dateRetour by remember { 
        mutableStateOf(
            existingVoyage?.date_retour?.let { dateFormat.format(it) } ?: ""
        ) 
    }
    var type by remember { mutableStateOf(existingVoyage?.type ?: "") }
    var prix by remember { mutableStateOf(existingVoyage?.prix_estime?.toString() ?: "") }
    var description by remember { mutableStateOf(existingVoyage?.description ?: "") }
    var placesDisponibles by remember { mutableStateOf(existingVoyage?.places_disponibles?.toString() ?: "") }
    var imageUrl by remember { mutableStateOf(existingVoyage?.imageUrl ?: "") }
    var imageUrlInput by remember { mutableStateOf("") }
    var showImageUrlDialog by remember { mutableStateOf(false) }
    
    // Date validation states
    var isDateDepartValid by remember { mutableStateOf(true) }
    var isDateRetourValid by remember { mutableStateOf(true) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Update image URL when in edit mode and existingVoyage changes
    LaunchedEffect(existingVoyage) {
        existingVoyage?.imageUrl?.let { url ->
            imageUrl = url
        }
    }
    
    // Function to handle image URL submission
    val onImageUrlSubmit = {
        if (imageUrlInput.isNotBlank()) {
            imageUrl = imageUrlInput
            showImageUrlDialog = false
            imageUrlInput = ""
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "URL de l'image mise à jour",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
    
    // Load voyage if editing
    LaunchedEffect(voyageId) {
        if (voyageId != null && existingVoyage == null && !isLoading) {
            viewModel.getVoyageById(voyageId) { result ->
                result.fold(
                    onSuccess = {
                        destination = it.destination
                        dateDepart = it.date_depart?.let { date -> dateFormat.format(date) } ?: ""
                        dateRetour = it.date_retour?.let { date -> dateFormat.format(date) } ?: ""
                        type = it.type
                        prix = it.prix_estime?.toString() ?: ""
                        description = it.description ?: ""
                        placesDisponibles = it.places_disponibles?.toString() ?: ""
                        imageUrl = it.imageUrl ?: ""
                    },
                    onFailure = {}
                )
            }
        }
    }
    
    // Show error if any
    error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMsg,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Modifier le voyage" else "Créer un voyage") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Voyage image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(ColorPrimary, ColorSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aucune image",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    // Image URL input dialog
                    if (showImageUrlDialog) {
                        AlertDialog(
                            onDismissRequest = { showImageUrlDialog = false },
                            title = { Text("URL de l'image") },
                            text = {
                                Column {
                                    Text("Entrez l'URL de l'image :")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = imageUrlInput,
                                        onValueChange = { imageUrlInput = it },
                                        label = { Text("URL de l'image") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = onImageUrlSubmit,
                                    enabled = imageUrlInput.isNotBlank()
                                ) {
                                    Text("Valider")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showImageUrlDialog = false }
                                ) {
                                    Text("Annuler")
                                }
                            }
                        )
                    }
                    
                    // Image URL input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showImageUrlDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ColorPrimary
                            )
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Définir l'URL de l'image")
                        }
                        
                        if (imageUrl.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    imageUrl = ""
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Image supprimée",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorError
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Supprimer")
                            }
                        }
                    }
                }
            }
            
            // Destination
            CustomTextField(
                value = destination,
                onValueChange = { destination = it },
                label = "Destination *",
                leadingIcon = Icons.Default.Place,
                modifier = Modifier.fillMaxWidth()
            )
            
            
            // Date Départ
            Column {
                CustomTextField(
                    value = dateDepart,
                    onValueChange = { 
                        dateDepart = it
                        isDateDepartValid = it.isEmpty() || isValidDateFormat(it)
                    },
                    label = "Date de départ * (YYYY-MM-DD HH:mm)",
                    leadingIcon = Icons.Default.CalendarToday,
                    placeholder = "2024-12-25 10:00",
                    isError = !isDateDepartValid,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isDateDepartValid) {
                    Text(
                        text = "Format de date invalide. Utilisez YYYY-MM-DD HH:mm",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            // Date Retour
            Column {
                CustomTextField(
                    value = dateRetour,
                    onValueChange = { 
                        dateRetour = it
                        isDateRetourValid = it.isEmpty() || isValidDateFormat(it)
                    },
                    label = "Date de retour * (YYYY-MM-DD HH:mm)",
                    leadingIcon = Icons.Default.CalendarToday,
                    placeholder = "2024-12-30 18:00",
                    isError = !isDateRetourValid,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isDateRetourValid) {
                    Text(
                        text = "Format de date invalide. Utilisez YYYY-MM-DD HH:mm",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            // Type dropdown
            var typeExpanded by remember { mutableStateOf(false) }
            val types = listOf("vol", "hôtel", "voiture")
            
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = type.ifEmpty { "Sélectionner un type" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type *") },
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null)
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorPrimary,
                        unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.5f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    types.forEach { typeOption ->
                        DropdownMenuItem(
                            text = { Text(typeOption) },
                            onClick = {
                                type = typeOption
                                typeExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Prix
            CustomTextField(
                value = prix,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() || it == '.' || it == ',' } && (newValue.isEmpty() || newValue.toDoubleOrNull() != null)) {
                        prix = newValue.replace(',', '.')
                    }
                },
                label = "Prix estimé (€)",
                leadingIcon = Icons.Default.AttachMoney,
                placeholder = "0.00",
                modifier = Modifier.fillMaxWidth()
            )
            
            // Places disponibles
            CustomTextField(
                value = placesDisponibles,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } && (newValue.isEmpty() || newValue.toIntOrNull() != null)) {
                        placesDisponibles = newValue
                    }
                },
                label = "Places disponibles",
                leadingIcon = Icons.Default.Person,
                placeholder = "10",
                modifier = Modifier.fillMaxWidth()
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorPrimary,
                    unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.5f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    // Validate form
                    if (destination.isBlank() || dateDepart.isBlank() || dateRetour.isBlank() || type.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Veuillez remplir tous les champs obligatoires (*)",
                                duration = SnackbarDuration.Long
                            )
                        }
                        return@Button
                    }

                    // Validate date format
                    isDateDepartValid = isValidDateFormat(dateDepart)
                    isDateRetourValid = isValidDateFormat(dateRetour)
                    
                    if (!isDateDepartValid || !isDateRetourValid) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Veuillez entrer des dates valides (format: AAAA-MM-JJ HH:MM)",
                                duration = SnackbarDuration.Long
                            )
                        }
                        return@Button
                    }
                    
                    try {
                        // Parse dates
                        val departDate = dateFormat.parse(dateDepart)
                        val retourDate = dateFormat.parse(dateRetour)
                        
                        if (departDate == null || retourDate == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Format de date invalide",
                                    duration = SnackbarDuration.Long
                                )
                            }
                            return@Button
                        }
                        
                        // Create the request object with all required fields
                        val request = com.travelmate.data.models.CreateVoyageRequest(
                            destination = destination.trim(),
                            date_depart = dateFormat.format(departDate),
                            date_retour = dateFormat.format(retourDate),
                            type = type.trim(),
                            description = description.takeIf { it.isNotBlank() },
                            prix_estime = prix.takeIf { it.isNotBlank() }?.toDoubleOrNull() ?: 0.0,
                            nombre_places = placesDisponibles.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 1,
                            imageUrl = imageUrl.takeIf { it.isNotBlank() }
                        )
                    
                        // Log the request for debugging
                        android.util.Log.d("VoyageFormScreen", "Submitting voyage: $request")
                    
                    if (isEditMode && voyageId != null) {
                        viewModel.updateVoyage(voyageId, request) { result ->
                            result.fold(
                                onSuccess = {
                                    onSuccess()
                                },
                                onFailure = { e ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Erreur lors de la mise à jour: ${e.message ?: "Erreur inconnue"}",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        viewModel.createVoyage(request) { result ->
                            result.fold(
                                onSuccess = {
                                    onSuccess()
                                },
                                onFailure = { e ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Erreur lors de la création: ${e.message ?: "Erreur inconnue"}",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            )
                        }
                    }
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Erreur lors du parsing des dates: ${e.message}",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && destination.isNotBlank() && dateDepart.isNotBlank() && 
                    dateRetour.isNotBlank() && type.isNotBlank() && isDateDepartValid && isDateRetourValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Modifier" else "Créer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

