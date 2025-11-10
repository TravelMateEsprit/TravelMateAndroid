package com.travelmate.ui.screens.user

import android.annotation.SuppressLint
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.travelmate.data.models.Voyage
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.VoyagesViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyageDetailScreen(
    voyageId: String,
    onNavigateBack: () -> Unit,
    onEditVoyage: (String) -> Unit,
    onDeleteVoyage: (String) -> Unit,
    voyagesViewModel: VoyagesViewModel = hiltViewModel()
) {
    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    val voyages by voyagesViewModel.voyages.collectAsState()
    val isLoading by voyagesViewModel.isLoading.collectAsState()
    val error by voyagesViewModel.error.collectAsState()
    
    val voyage = remember(voyageId, voyages) {
        voyages.find { it.id_voyage == voyageId }
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Load voyage if not found
    LaunchedEffect(voyageId) {
        if (voyage == null && !isLoading) {
            voyagesViewModel.getVoyageById(voyageId) {}
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
                voyagesViewModel.clearError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du voyage") },
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
        if (isLoading && voyage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorPrimary)
            }
        } else if (voyage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Voyage introuvable",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Retour")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Voyage Details Card
                VoyageDetailCard(voyage = voyage)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onEditVoyage(voyage.id_voyage) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ColorPrimary
                        )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modifier",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorError
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Supprimer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(
                    text = "Supprimer le voyage",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Êtes-vous sûr de vouloir supprimer ce voyage ? Cette action est irréversible.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        voyage?.let { v ->
                            scope.launch {
                                voyagesViewModel.deleteVoyage(v.id_voyage) { result ->
                                    scope.launch {
                                        if (result.isSuccess) {
                                            snackbarHostState.showSnackbar(
                                                message = "Voyage supprimé avec succès",
                                                duration = SnackbarDuration.Short
                                            )
                                            onNavigateBack()
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = "Erreur lors de la suppression: ${result.exceptionOrNull()?.message}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorError
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
@SuppressLint("SimpleDateFormat")
fun VoyageDetailCard(voyage: Voyage) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val dateDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    var dateDepartText by remember { mutableStateOf(voyage.date_depart) }
    var dateRetourText by remember { mutableStateOf(voyage.date_retour) }
    var timeDepartText by remember { mutableStateOf("") }
    var timeRetourText by remember { mutableStateOf("") }
    
    LaunchedEffect(voyage) {
        try {
            val depart = dateFormat.parse(voyage.date_depart)
            val retour = dateFormat.parse(voyage.date_retour)
            
            depart?.let {
                dateDepartText = dateDisplayFormat.format(it)
                timeDepartText = timeFormat.format(it)
            }
            retour?.let {
                dateRetourText = dateDisplayFormat.format(it)
                timeRetourText = timeFormat.format(it)
            }
        } catch (e: Exception) {
            // Keep original if parsing fails
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                if (!voyage.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = voyage.imageUrl,
                        contentDescription = voyage.destination,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ColorPrimary, ColorSecondary)
                                )
                            )
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Flight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                
                // Owner Badge
                Surface(
                    color = ColorAccent,
                    shape = RoundedCornerShape(bottomStart = 16.dp, topEnd = 20.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Mon voyage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Content Section
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Destination
                Text(
                    text = voyage.destination,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Type
                Surface(
                    color = ColorPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = voyage.type,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Details Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = ColorBackground,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Départ
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Flight,
                                    contentDescription = null,
                                    tint = ColorPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Départ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dateDepartText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            if (timeDepartText.isNotEmpty()) {
                                Text(
                                    text = timeDepartText,
                                    fontSize = 14.sp,
                                    color = ColorTextSecondary
                                )
                            }
                        }
                        
                        // Arrow
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = ColorTextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 24.dp)
                        )
                        
                        // Retour
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Retour",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorTextSecondary
                                )
                                Icon(
                                    Icons.Default.Flight,
                                    contentDescription = null,
                                    tint = ColorPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dateRetourText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            if (timeRetourText.isNotEmpty()) {
                                Text(
                                    text = timeRetourText,
                                    fontSize = 14.sp,
                                    color = ColorTextSecondary
                                )
                            }
                        }
                    }
                    
                    // Divider
                    HorizontalDivider(
                        color = ColorTextSecondary.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    
                    // Prix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = ColorAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Prix estimé",
                                fontSize = 14.sp,
                                color = ColorTextSecondary
                            )
                        }
                        Text(
                            text = "${(voyage.prix_estime ?: 0.0).toInt()} €",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                // Description
                if (!voyage.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = voyage.description,
                        fontSize = 14.sp,
                        color = ColorTextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
