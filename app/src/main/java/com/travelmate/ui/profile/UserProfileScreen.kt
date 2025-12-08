package com.travelmate.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmate.data.models.UpdateProfileRequest
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

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
    
    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ColorBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorPrimary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header avec avatar
                    UserProfileHeader(
                        name = user?.name ?: "Utilisateur",
                        email = user?.email ?: "",
                        isEditing = isEditing,
                        onEditClick = { isEditing = !isEditing }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Section Informations personnelles
                        UserProfileSectionCard(
                            title = "Informations personnelles",
                            icon = Icons.Outlined.Person
                        ) {
                            if (isEditing) {
                                UserModernTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = "Nom complet",
                                    icon = Icons.Outlined.Person
                                )
                                UserModernTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = "Telephone",
                                    icon = Icons.Outlined.Phone
                                )
                                UserModernTextField(
                                    value = bio,
                                    onValueChange = { bio = it },
                                    label = "Bio",
                                    icon = Icons.Outlined.Info,
                                    minLines = 3
                                )
                            } else {
                                UserProfileInfoRow(icon = Icons.Outlined.Person, label = "Nom", value = name.ifEmpty { "Non renseigne" })
                                UserProfileInfoRow(icon = Icons.Outlined.Phone, label = "Telephone", value = phone.ifEmpty { "Non renseigne" })
                                UserProfileInfoRow(icon = Icons.Outlined.Info, label = "Bio", value = bio.ifEmpty { "Non renseigne" })
                            }
                        }

                        // Section Adresse
                        UserProfileSectionCard(
                            title = "Adresse",
                            icon = Icons.Outlined.LocationOn
                        ) {
                            if (isEditing) {
                                UserModernTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = "Adresse",
                                    icon = Icons.Outlined.Home
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        UserModernTextField(
                                            value = city,
                                            onValueChange = { city = it },
                                            label = "Ville",
                                            icon = Icons.Outlined.LocationCity
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        UserModernTextField(
                                            value = country,
                                            onValueChange = { country = it },
                                            label = "Pays",
                                            icon = Icons.Outlined.Public
                                        )
                                    }
                                }
                            } else {
                                UserProfileInfoRow(icon = Icons.Outlined.Home, label = "Adresse", value = address.ifEmpty { "Non renseigne" })
                                UserProfileInfoRow(icon = Icons.Outlined.LocationCity, label = "Ville", value = city.ifEmpty { "Non renseigne" })
                                UserProfileInfoRow(icon = Icons.Outlined.Public, label = "Pays", value = country.ifEmpty { "Non renseigne" })
                            }
                        }

                        // Boutons d'action
                        AnimatedVisibility(
                            visible = isEditing,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
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
                                            snackbarHostState.showSnackbar("Profil mis a jour avec succes")
                                        }
                                        isEditing = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Enregistrer les modifications", fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        user?.let {
                                            name = it.name ?: ""
                                            phone = it.phone ?: ""
                                            address = it.address ?: ""
                                            city = it.city ?: ""
                                            country = it.country ?: ""
                                            bio = it.bio ?: ""
                                        }
                                        isEditing = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTextSecondary)
                                ) {
                                    Text("Annuler", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Bouton de deconnexion
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorError),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(ColorError, ColorError))
                            )
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Se deconnecter", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Dialog de confirmation de deconnexion
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = ColorError) },
                    title = { Text("Deconnexion", fontWeight = FontWeight.Bold) },
                    text = { Text("Etes-vous sur de vouloir vous deconnecter ?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                        ) {
                            Text("Se deconnecter")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Annuler")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun UserProfileHeader(
    name: String,
    email: String,
    isEditing: Boolean,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ColorPrimary, ColorPrimary.copy(alpha = 0.8f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                val initials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
                Text(
                    text = initials.ifEmpty { "U" },
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name.ifEmpty { "Utilisateur" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = email,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton Modifier
            Surface(
                modifier = Modifier.clickable { onEditClick() },
                shape = RoundedCornerShape(24.dp),
                color = if (isEditing) Color.White else Color.White.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (isEditing) ColorPrimary else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Annuler" else "Modifier le profil",
                        color = if (isEditing) ColorPrimary else Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
            }
            Divider(color = ColorDivider, thickness = 1.dp)
            content()
        }
    }
}

@Composable
private fun UserProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ColorPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ColorPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = ColorTextSecondary
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ColorTextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ColorPrimary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ColorPrimary,
            unfocusedBorderColor = ColorDivider,
            focusedLabelColor = ColorPrimary,
            cursorColor = ColorPrimary
        ),
        minLines = minLines
    )
}

@Composable
private fun UserProfileNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ColorPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ColorPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = ColorTextSecondary
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = ColorTextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}
