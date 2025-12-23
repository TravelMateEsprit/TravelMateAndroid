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
import com.travelmate.data.models.UpdateAgencyProfileRequest
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val user by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var name by remember { mutableStateOf("") }
    var agencyName by remember { mutableStateOf("") }
    var agencyLicense by remember { mutableStateOf("") }
    var agencyWebsite by remember { mutableStateOf("") }
    var agencyDescription by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    var isEditing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user) {
        user?.let {
            name = it.name ?: ""
            agencyName = it.agencyName ?: ""
            agencyLicense = it.agencyLicense ?: ""
            agencyWebsite = it.agencyWebsite ?: ""
            agencyDescription = it.agencyDescription ?: ""
            phone = it.phone ?: ""
            address = it.address ?: ""
            city = it.city ?: ""
            country = it.country ?: ""
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ColorBackground,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    // Header avec logo agence
                    AgencyProfileHeader(
                        agencyName = agencyName.ifEmpty { "Mon Agence" },
                        email = user?.email ?: "",
                        isVerified = user?.isAgencyVerified == true,
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

                        // Statut de verification
                        AgencyStatusCard(isVerified = user?.isAgencyVerified == true)

                        // Section Informations de l'agence
                        AgencyProfileSectionCard(
                            title = "Informations de l'agence",
                            icon = Icons.Outlined.Business
                        ) {
                            if (isEditing) {
                                AgencyModernTextField(
                                    value = agencyName,
                                    onValueChange = { agencyName = it },
                                    label = "Nom de l'agence",
                                    icon = Icons.Outlined.Business
                                )
                                AgencyModernTextField(
                                    value = agencyLicense,
                                    onValueChange = { agencyLicense = it },
                                    label = "Numero de licence",
                                    icon = Icons.Outlined.Badge
                                )
                                AgencyModernTextField(
                                    value = agencyWebsite,
                                    onValueChange = { agencyWebsite = it },
                                    label = "Site Web",
                                    icon = Icons.Outlined.Language
                                )
                                AgencyModernTextField(
                                    value = agencyDescription,
                                    onValueChange = { agencyDescription = it },
                                    label = "Description",
                                    icon = Icons.Outlined.Description,
                                    minLines = 3
                                )
                            } else {
                                AgencyProfileInfoRow(icon = Icons.Outlined.Business, label = "Nom de l'agence", value = agencyName.ifEmpty { "Non renseigne" })
                                AgencyProfileInfoRow(icon = Icons.Outlined.Badge, label = "Licence", value = agencyLicense.ifEmpty { "Non renseigne" })
                                AgencyProfileInfoRow(icon = Icons.Outlined.Language, label = "Site Web", value = agencyWebsite.ifEmpty { "Non renseigne" })
                                AgencyProfileInfoRow(icon = Icons.Outlined.Description, label = "Description", value = agencyDescription.ifEmpty { "Non renseigne" })
                            }
                        }

                        // Section Responsable
                        AgencyProfileSectionCard(
                            title = "Responsable",
                            icon = Icons.Outlined.Person
                        ) {
                            if (isEditing) {
                                AgencyModernTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = "Nom du responsable",
                                    icon = Icons.Outlined.Person
                                )
                                AgencyModernTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = "Telephone",
                                    icon = Icons.Outlined.Phone
                                )
                            } else {
                                AgencyProfileInfoRow(icon = Icons.Outlined.Person, label = "Responsable", value = name.ifEmpty { "Non renseigne" })
                                AgencyProfileInfoRow(icon = Icons.Outlined.Phone, label = "Telephone", value = phone.ifEmpty { "Non renseigne" })
                                AgencyProfileInfoRow(icon = Icons.Outlined.Email, label = "Email", value = user?.email ?: "Non renseigne")
                            }
                        }

                        // Section Adresse
                        AgencyProfileSectionCard(
                            title = "Localisation",
                            icon = Icons.Outlined.LocationOn
                        ) {
                            if (isEditing) {
                                AgencyModernTextField(
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
                                        AgencyModernTextField(
                                            value = city,
                                            onValueChange = { city = it },
                                            label = "Ville",
                                            icon = Icons.Outlined.LocationCity
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        AgencyModernTextField(
                                            value = country,
                                            onValueChange = { country = it },
                                            label = "Pays",
                                            icon = Icons.Outlined.Public
                                        )
                                    }
                                }
                            } else {
                                AgencyProfileInfoRow(icon = Icons.Outlined.Home, label = "Adresse", value = address.ifEmpty { "Non renseigne" })
                                AgencyProfileInfoRow(icon = Icons.Outlined.LocationCity, label = "Ville", value = city.ifEmpty { "Non renseigne" })
                                AgencyProfileInfoRow(icon = Icons.Outlined.Public, label = "Pays", value = country.ifEmpty { "Non renseigne" })
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
                                        viewModel.updateAgencyProfile(
                                            UpdateAgencyProfileRequest(
                                                name = name,
                                                agencyName = agencyName,
                                                agencyLicense = agencyLicense,
                                                agencyWebsite = agencyWebsite,
                                                agencyDescription = agencyDescription,
                                                phone = phone,
                                                address = address,
                                                city = city,
                                                country = country
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
                                            agencyName = it.agencyName ?: ""
                                            agencyLicense = it.agencyLicense ?: ""
                                            agencyWebsite = it.agencyWebsite ?: ""
                                            agencyDescription = it.agencyDescription ?: ""
                                            phone = it.phone ?: ""
                                            address = it.address ?: ""
                                            city = it.city ?: ""
                                            country = it.country ?: ""
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

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AgencyProfileHeader(
    agencyName: String,
    email: String,
    isVerified: Boolean,
    isEditing: Boolean,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A5F),
                        Color(0xFF2E5077)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Agence
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Color(0xFF1E3A5F),
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = agencyName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isVerified) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verifie",
                        tint = ColorSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

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
                    modifier = Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (isEditing) Color(0xFF1E3A5F) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Annuler" else "Modifier le profil",
                        color = if (isEditing) Color(0xFF1E3A5F) else Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AgencyStatusCard(isVerified: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVerified) ColorSuccess.copy(alpha = 0.1f) else ColorWarning.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isVerified) ColorSuccess.copy(alpha = 0.2f) else ColorWarning.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Pending,
                    contentDescription = null,
                    tint = if (isVerified) ColorSuccess else ColorWarning,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isVerified) "Agence verifiee" else "En attente de verification",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isVerified) ColorSuccess else ColorWarning
                )
                Text(
                    text = if (isVerified) "Votre agence est officiellement verifiee" else "Votre demande est en cours de traitement",
                    fontSize = 13.sp,
                    color = ColorTextSecondary
                )
            }
        }
    }
}

@Composable
private fun AgencyProfileSectionCard(
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
                    tint = Color(0xFF1E3A5F),
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
private fun AgencyProfileInfoRow(
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
                .background(Color(0xFF1E3A5F).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF1E3A5F),
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
private fun AgencyModernTextField(
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
                tint = Color(0xFF1E3A5F)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1E3A5F),
            unfocusedBorderColor = ColorDivider,
            focusedLabelColor = Color(0xFF1E3A5F),
            cursorColor = Color(0xFF1E3A5F)
        ),
        minLines = minLines
    )
}
