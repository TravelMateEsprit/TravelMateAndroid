package com.travelmate.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.data.models.UpdateProfileRequest
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * User Profile Screen moderne inspiré des apps de voyage
 * Design épuré, spacieux et actionnable
 * Édition inline avec animations fluides
 * Compatible Dark Mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    navController: androidx.navigation.NavController? = null,
    onLogout: () -> Unit = {}
) {
    val user by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    
    var editingField by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCopyToast by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(user) {
        user?.let {
            name = it.name ?: ""
            phone = it.phone ?: ""
            address = it.address ?: ""
            city = it.city ?: ""
            country = it.country ?: ""
            bio = it.bio ?: ""
        }
    }

    LaunchedEffect(error) {
        error?.let {
            scope.launch {
            snackbarHostState.showSnackbar(it)
            }
        }
    }

    LaunchedEffect(showCopyToast) {
        if (showCopyToast) {
            delay(2000)
            showCopyToast = false
        }
    }
    
    // Observer le loading pour afficher le loader moderne
    LaunchedEffect(isLoading) {
        if (isLoading && editingField != null) {
            isSaving = true
        } else {
            delay(500) // Petit délai pour l'animation
            isSaving = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        if (isLoading && user == null) {
            ModernProfileSkeleton(colorScheme = colorScheme)
            } else {
            LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                // Header moderne avec avatar éditable
                item {
                    ModernProfileHeader(
                        name = user?.name ?: "Utilisateur",
                        email = user?.email ?: "",
                        userType = user?.userType ?: "user",
                        onCopyEmail = {
                            copyToClipboard(context, user?.email ?: "", "Email copié")
                            showCopyToast = true
                        },
                        colorScheme = colorScheme
                    )
                }
                
                // Section Informations de base - Design épuré
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    ModernInfoSection(
                        title = "Informations de base",
                        items = listOf(
                            ProfileField(
                                key = "name",
                                    label = "Nom complet",
                                value = name.ifEmpty { "Non renseigné" },
                                icon = Icons.Outlined.Person,
                                isEditable = true,
                                onEdit = { editingField = "name" },
                                onSave = { newValue ->
                                    name = newValue
                                    editingField = null
                                    saveProfile(viewModel, name, phone, address, city, country, bio, scope, snackbarHostState)
                                },
                                onCancel = { editingField = null }
                            ),
                            ProfileField(
                                key = "email",
                                label = "Email",
                                value = user?.email ?: "",
                                icon = Icons.Outlined.Email,
                                isEditable = false,
                                onCopy = {
                                    copyToClipboard(context, user?.email ?: "", "Email copié")
                                    showCopyToast = true
                                }
                            ),
                            ProfileField(
                                key = "phone",
                                label = "Téléphone",
                                value = phone.ifEmpty { "Non renseigné" },
                                icon = Icons.Outlined.Phone,
                                isEditable = true,
                                onEdit = { editingField = "phone" },
                                onSave = { newValue ->
                                    phone = newValue
                                    editingField = null
                                    saveProfile(viewModel, name, phone, address, city, country, bio, scope, snackbarHostState)
                                },
                                onCancel = { editingField = null },
                                onCopy = if (phone.isNotEmpty()) {
                                    {
                                        copyToClipboard(context, phone, "Téléphone copié")
                                        showCopyToast = true
                                    }
                                } else null
                            )
                        ),
                        editingField = editingField,
                        isSaving = isSaving && editingField != null,
                        colorScheme = colorScheme
                    )
                }
                
                // Section À propos
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ModernInfoSection(
                        title = "À propos",
                        items = listOf(
                            ProfileField(
                                key = "bio",
                                    label = "Bio",
                                value = bio.ifEmpty { "Ajoutez une description de vous" },
                                    icon = Icons.Outlined.Info,
                                isEditable = true,
                                isMultiline = true,
                                onEdit = { editingField = "bio" },
                                onSave = { newValue ->
                                    bio = newValue
                                    editingField = null
                                    saveProfile(viewModel, name, phone, address, city, country, bio, scope, snackbarHostState)
                                },
                                onCancel = { editingField = null }
                            )
                        ),
                        editingField = editingField,
                        isSaving = isSaving && editingField == "bio",
                        colorScheme = colorScheme
                    )
                        }

                        // Section Adresse
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ModernInfoSection(
                            title = "Adresse",
                        items = listOf(
                            ProfileField(
                                key = "address",
                                    label = "Adresse",
                                value = address.ifEmpty { "Non renseigné" },
                                icon = Icons.Outlined.Home,
                                isEditable = true,
                                onEdit = { editingField = "address" },
                                onSave = { newValue ->
                                    address = newValue
                                    editingField = null
                                    saveProfile(viewModel, name, phone, address, city, country, bio, scope, snackbarHostState)
                                },
                                onCancel = { editingField = null }
                            ),
                            ProfileField(
                                key = "city",
                                            label = "Ville",
                                value = city.ifEmpty { "Non renseigné" },
                                icon = Icons.Outlined.LocationCity,
                                isEditable = true,
                                onEdit = { editingField = "city" },
                                onSave = { newValue ->
                                    city = newValue
                                    editingField = null
                                    saveProfile(viewModel, name, phone, address, city, country, bio, scope, snackbarHostState)
                                },
                                onCancel = { editingField = null }
                            ),
                            ProfileField(
                                key = "country",
                                label = "Pays",
                                value = country.ifEmpty { "Non renseigné" },
                                icon = Icons.Outlined.Public,
                                isEditable = true,
                                onEdit = { editingField = "country" },
                                onSave = { newValue ->
                                    country = newValue
                                    editingField = null
                                    saveProfile(viewModel, name, phone, address, city, country, bio, scope, snackbarHostState)
                                },
                                onCancel = { editingField = null }
                            )
                        ),
                        editingField = editingField,
                        isSaving = isSaving && editingField != null && 
                            (editingField == "address" || editingField == "city" || editingField == "country"),
                        colorScheme = colorScheme
                    )
                }
                
                // Actions rapides - Nouveau design
                navController?.let { nav ->
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        RedesignedQuickActions(
                            onMyRequests = {
                                nav.navigate(com.travelmate.utils.Constants.Routes.MY_INSURANCE_REQUESTS)
                            },
                            onMyClaims = {
                                nav.navigate(com.travelmate.utils.Constants.Routes.MY_CLAIMS)
                            },
                            colorScheme = colorScheme
                        )
                    }
                }
                
                // Bouton de déconnexion
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    ModernLogoutButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.padding(horizontal = 20.dp),
                        colorScheme = colorScheme
                    )
                        }

                // Espace final
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    
    // Loading overlay moderne lors de la sauvegarde
    if (isSaving) {
        ModernSavingOverlay(colorScheme = colorScheme)
    }
    
    // Toast pour copie
    if (showCopyToast) {
        Box(
                            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.primary.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Copié dans le presse-papiers",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                        }
                    }
                }
            }

    // Dialog de confirmation de déconnexion
            if (showLogoutDialog) {
        ModernAlertDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                                showLogoutDialog = false
                                onLogout()
                            },
            title = "Déconnexion",
            message = "Êtes-vous sûr de vouloir vous déconnecter ?",
            confirmText = "Se déconnecter",
            isDestructive = true,
            colorScheme = colorScheme
        )
                        }
}

/**
 * Data class pour les champs de profil
 */
data class ProfileField(
    val key: String,
    val label: String,
    val value: String,
    val icon: ImageVector,
    val isEditable: Boolean = false,
    val isMultiline: Boolean = false,
    val onEdit: (() -> Unit)? = null,
    val onSave: ((String) -> Unit)? = null,
    val onCancel: (() -> Unit)? = null,
    val onCopy: (() -> Unit)? = null
)

/**
 * Header moderne avec avatar grand et éditable
 */
@Composable
private fun ModernProfileHeader(
    name: String,
    email: String,
    userType: String,
    onCopyEmail: () -> Unit,
    colorScheme: ColorScheme
) {
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { "U" }
    
        Column(
            modifier = Modifier
                .fillMaxWidth()
            .background(colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Espace pour la status bar
        Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Avatar grand avec possibilité de changer
        Box {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = colorScheme.primary.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 4.dp,
                    color = colorScheme.surface
                ),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                        text = initials,
                        fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }
            }
            
            // Bouton pour changer la photo (futur)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp),
                shape = CircleShape,
                color = colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Changer la photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            }

        Spacer(modifier = Modifier.height(20.dp))

        // Nom
            Text(
                text = name.ifEmpty { "Utilisateur" },
            fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Email avec copie
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = email,
                fontSize = 15.sp,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onCopyEmail,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = "Copier l'email",
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Badge type utilisateur
            Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (userType.lowercase() == "agence") {
                colorScheme.tertiary.copy(alpha = 0.15f)
            } else {
                colorScheme.primary.copy(alpha = 0.15f)
            }
            ) {
                Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                    if (userType.lowercase() == "agence") Icons.Default.Business else Icons.Default.Person,
                        contentDescription = null,
                    tint = if (userType.lowercase() == "agence") colorScheme.tertiary else colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                    )
                    Text(
                    text = if (userType.lowercase() == "agence") "Agence" else "Utilisateur",
                    fontSize = 13.sp,
                    color = if (userType.lowercase() == "agence") colorScheme.tertiary else colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
                }
            }

/**
 * Section d'informations moderne avec édition inline
 */
@Composable
private fun ModernInfoSection(
    title: String,
    items: List<ProfileField>,
    editingField: String?,
    isSaving: Boolean,
    colorScheme: ColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Titre de section
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Items
        items.forEach { field ->
            ModernProfileFieldItem(
                field = field,
                isEditing = editingField == field.key,
                isSaving = isSaving && editingField == field.key,
                colorScheme = colorScheme
            )
            
            if (field != items.last()) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Item de champ de profil moderne avec édition inline et loading
 */
@Composable
private fun ModernProfileFieldItem(
    field: ProfileField,
    isEditing: Boolean,
    isSaving: Boolean,
    colorScheme: ColorScheme
) {
    var tempValue by remember(field.value, isEditing) { mutableStateOf(field.value) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box {
            // Overlay de loading moderne
            if (isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Enregistrement...",
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            if (isEditing) {
                // Mode édition
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                Icon(
                            imageVector = field.icon,
                    contentDescription = null,
                            tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                            text = field.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
                    }
                    
                    if (field.isMultiline) {
                        OutlinedTextField(
                            value = tempValue,
                            onValueChange = { tempValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline,
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface
                            ),
                            enabled = !isSaving
                        )
                    } else {
                        OutlinedTextField(
                            value = tempValue,
                            onValueChange = { tempValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline,
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface
                            ),
                            enabled = !isSaving
                        )
                    }
                    
                    // Boutons d'action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { field.onCancel?.invoke() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSaving,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.onSurface
                            )
                        ) {
                            Text("Annuler")
                        }
                        
                        Button(
                            onClick = { field.onSave?.invoke(tempValue) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Enregistrer", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                // Mode affichage
    Row(
        modifier = Modifier
            .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Box(
                                modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                                    imageVector = field.icon,
                contentDescription = null,
                                    tint = colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
            Text(
                                text = field.label,
                                fontSize = 13.sp,
                                color = colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
            )
                            Spacer(modifier = Modifier.height(4.dp))
            Text(
                                text = field.value,
                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (field.value == "Non renseigné" || field.value.contains("Ajoutez")) {
                                    colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                } else {
                                    colorScheme.onSurface
                                }
                            )
                        }
                    }
                    
                    // Actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (field.onCopy != null && field.value.isNotEmpty() && 
                            field.value != "Non renseigné" && !field.value.contains("Ajoutez")) {
                            IconButton(
                                onClick = field.onCopy,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = "Copier",
                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        if (field.isEditable) {
                            IconButton(
                                onClick = { field.onEdit?.invoke() },
                                modifier = Modifier.size(40.dp)
                            ) {
            Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Modifier",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Actions rapides redessinées - Nouvelle approche
 */
@Composable
private fun RedesignedQuickActions(
    onMyRequests: () -> Unit,
    onMyClaims: () -> Unit,
    colorScheme: ColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Actions rapides",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Design horizontal avec icônes et flèches
        Card(
        modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column {
                // Mes demandes
                Surface(
                    onClick = onMyRequests,
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Assignment,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
    )
}
                            }
                            
                            Column {
                                Text(
                                    text = "Mes demandes",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Suivre vos demandes d'assurance",
                                    fontSize = 13.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Ouvrir",
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = colorScheme.outline.copy(alpha = 0.3f)
                )
                
                // Mes réclamations
                Surface(
                    onClick = onMyClaims,
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = colorScheme.tertiary.copy(alpha = 0.1f)
    ) {
        Box(
                                    modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                                        Icons.Outlined.Report,
                contentDescription = null,
                                        tint = colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
        }
                            }
                            
                            Column {
            Text(
                                    text = "Mes réclamations",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                                    text = "Gérer vos réclamations",
                fontSize = 13.sp,
                                    color = colorScheme.onSurfaceVariant
            )
        }
                        }
                        
        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Ouvrir",
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
    }
                }
            }
        }
    }
}

/**
 * Bouton de déconnexion moderne
 */
@Composable
private fun ModernLogoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorScheme.error
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.5.dp
        )
    ) {
        Icon(
            Icons.Default.Logout,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            "Se déconnecter",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

/**
 * Overlay de chargement moderne lors de la sauvegarde
 */
@Composable
private fun ModernSavingOverlay(colorScheme: ColorScheme) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorScheme.surface.copy(alpha = 0.85f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Animation de chargement avec rotation
                val infiniteTransition = rememberInfiniteTransition(label = "loading")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )
                
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier
                            .size(28.dp)
                            .scale(rotation / 360f)
                    )
                }
                
                Text(
                    text = "Enregistrement en cours...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                
                Text(
                    text = "Veuillez patienter",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Dialog moderne
 */
@Composable
private fun ModernAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    isDestructive: Boolean = false,
    colorScheme: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (isDestructive) colorScheme.error.copy(alpha = 0.1f) else colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = if (isDestructive) colorScheme.error else colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface
            )
        },
        text = {
            Text(
                message,
                color = colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) colorScheme.error else colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Annuler")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = colorScheme.surface
    )
}

/**
 * Skeleton loader moderne
 */
@Composable
private fun ModernProfileSkeleton(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )
        
        // Cards skeleton
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.2f))
            )
        }
    }
}

/**
 * Helper function pour sauvegarder le profil
 */
private fun saveProfile(
    viewModel: ProfileViewModel,
    name: String,
    phone: String,
    address: String,
    city: String,
    country: String,
    bio: String,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    viewModel.updateProfile(
        UpdateProfileRequest(
            name = name,
            phone = phone,
            address = address,
            city = city,
            country = country,
            bio = bio
        )
    )
    scope.launch {
        snackbarHostState.showSnackbar("Profil mis à jour avec succès")
    }
}

/**
 * Helper function pour copier dans le presse-papiers
 */
private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
