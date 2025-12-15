package com.travelmate.ui.screens.agency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.data.models.Pack
import com.travelmate.ui.components.ModernCard
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.AgencyPacksViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackDetailsScreen(
    packId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: AgencyPacksViewModel = hiltViewModel()
) {
    val packs by viewModel.packs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val pack = packs.find { it.id == packId }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showToggleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(packId) {
        if (pack == null) {
            viewModel.loadMyPacks()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Êtes-vous sûr de vouloir supprimer ce pack ?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePack(packId)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Toggle active/inactive dialog
    if (showToggleDialog && pack != null) {
        AlertDialog(
            onDismissRequest = { showToggleDialog = false },
            title = { Text(if (pack.actif) "Désactiver" else "Activer") },
            text = {
                Text(
                    if (pack.actif) "Voulez-vous désactiver ce pack ?"
                    else "Voulez-vous activer ce pack ?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Toggle active status
                        val updateRequest = com.travelmate.data.models.UpdatePackRequest(
                            actif = !pack.actif
                        )
                        viewModel.updatePack(packId, updateRequest)
                        showToggleDialog = false
                    }
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showToggleDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du pack") },
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
        // Fixed bottom buttons
        bottomBar = {
            if (pack != null) {
                Surface(
                    shadowElevation = 16.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Modifier button
                        Button(
                            onClick = { onNavigateToEdit(packId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Modifier")
                        }

                        // Toggle Active/Inactive button
                        Button(
                            onClick = { showToggleDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (pack.actif) Color(0xFFFF9800)
                                else ColorSuccess
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (pack.actif) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (pack.actif) "Désactiver" else "Activer")
                        }

                        // Supprimer button
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorError
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Supprimer")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading || (pack == null && packs.isEmpty()) -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorPrimary)
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = ColorError,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Erreur",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            error ?: "Une erreur est survenue",
                            fontSize = 14.sp,
                            color = ColorTextSecondary
                        )
                    }
                }
            }

            pack == null && packs.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = ColorTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Pack introuvable")
                    }
                }
            }

            else -> {
                PackDetailsContent(
                    pack = pack!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun PackDetailsContent(
    pack: Pack,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(ColorPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            pack.titre,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = if (pack.actif) ColorSuccess.copy(alpha = 0.2f)
                            else ColorTextSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pack.actif) ColorSuccess else ColorTextSecondary
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (pack.actif) "Actif" else "Inactif",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (pack.actif) ColorSuccess else ColorTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Agency Info Card
        if (pack.agenceInfo != null) {
            ModernCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Agence",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DetailRow(label = "Nom", value = pack.agenceInfo!!.agencyName ?: pack.agenceInfo!!.name ?: "-")
                    DetailRow(label = "Email", value = pack.agenceInfo!!.email ?: "-")
                    DetailRow(label = "Téléphone", value = pack.agenceInfo!!.phone ?: "-")
                    DetailRow(label = "Adresse", value = pack.agenceInfo!!.address ?: "-")
                    DetailRow(label = "Licence", value = pack.agenceInfo!!.agencyLicense ?: "-")
                }
            }
        }

        // Price Card
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Prix (Adulte)",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                    Text(
                        "${pack.prix.toInt()} DT / nuit",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary
                    )
                    if (pack.priceChild != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Enfant: ${pack.priceChild?.toInt() ?: 0} DT / nuit",
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )
                    }
                }
            }
        }

        // Destination Card
        DetailInfoCard(
            icon = Icons.Default.Place,
            label = "Destination",
            value = pack.destination ?: "Non spécifiée"
        )

        // Dates Card
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Dates",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Début", fontSize = 12.sp, color = ColorTextSecondary)
                        Text(
                            formatDate(pack.dateDebut),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = ColorTextSecondary)
                    Column {
                        Text("Fin", fontSize = 12.sp, color = ColorTextSecondary)
                        Text(
                            formatDate(pack.dateFin),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Type d'offre
        pack.typeOffre?.let { typeOffre ->
            DetailInfoCard(
                icon = Icons.Default.Category,
                label = "Type d'offre",
                value = typeOffre.replaceFirstChar { it.uppercase() }
            )
        }

        // Places disponibles
        pack.placesDisponibles?.let { places ->
            DetailInfoCard(
                icon = Icons.Default.People,
                label = "Places disponibles",
                value = "$places personnes"
            )
        }

        // Description Card
        if (pack.description.isNotEmpty()) {
            ModernCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = ColorPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        pack.description,
                        fontSize = 14.sp,
                        color = ColorTextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Images (if any)
        if (pack.images.isNotEmpty()) {
            ModernCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = ColorPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Images (${pack.images.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        pack.images.joinToString("\n"),
                        fontSize = 12.sp,
                        color = ColorTextSecondary
                    )
                }
            }
        }

        // Bottom spacing for fixed buttons
        Spacer(modifier = Modifier.height(80.dp))
    }
}


@Composable
fun DetailInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = ColorPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    label,
                    fontSize = 12.sp,
                    color = ColorTextSecondary
                )
                Text(
                    value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextPrimary
                )
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = ColorTextSecondary,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ColorTextPrimary,
            modifier = Modifier.weight(0.7f),
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}
