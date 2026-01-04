package com.travelmate.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.*
import com.travelmate.ui.viewmodels.CompleteProfileViewModel

@Composable
fun CompleteProfileScreen(
    navController: NavController,
    viewModel: CompleteProfileViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val canProceed by viewModel.canProceedToNextStep.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    // Charger le profil existant au démarrage si disponible
    LaunchedEffect(Unit) {
        viewModel.loadExistingProfile()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.popBackStack()
        }
    }

    errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header avec gradient
            ProfileHeader(
                currentStep = currentStep,
                isEditMode = isEditMode,
                onBackClick = {
                    if (currentStep > 0) {
                        viewModel.previousStep()
                    } else {
                        navController.popBackStack()
                    }
                }
            )

            // Stepper moderne
            ModernStepper(
                currentStep = currentStep,
                totalSteps = 4
            )

            // Contenu selon l'étape
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    },
                    label = "stepTransition"
                ) { step ->
                    when (step) {
                        0 -> Step1_AgeFrequency(viewModel)
                        1 -> Step2_Destinations(viewModel)
                        2 -> Step3_BudgetPurpose(viewModel)
                        3 -> Step4_CompanionHealth(viewModel)
                    }
                }
            }

            // Footer avec boutons
            ProfileFooter(
                currentStep = currentStep,
                canProceed = canProceed,
                isLoading = isLoading,
                onPreviousClick = { viewModel.previousStep() },
                onNextClick = {
                    if (currentStep < 3) {
                        viewModel.nextStep()
                    } else {
                        viewModel.submitProfile()
                    }
                }
            )
        }

        // Snackbar pour les erreurs
        errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun ProfileHeader(
    currentStep: Int,
    onBackClick: () -> Unit,
    isEditMode: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Retour",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (isEditMode) Icons.Default.Edit else Icons.Default.TravelExplore,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (isEditMode) "Modifiez votre profil" else "Complétez votre profil",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Pour des recommandations personnalisées",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun ModernStepper(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            StepIndicator(
                stepNumber = index + 1,
                isActive = index == currentStep,
                isCompleted = index < currentStep,
                modifier = Modifier.weight(1f)
            )
            if (index < totalSteps - 1) {
                StepConnector(
                    isActive = index < currentStep,
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StepIndicator(
    stepNumber: Int,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.primary
            isActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "stepColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        label = "stepScale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .scale(scale),
            shape = CircleShape,
            color = backgroundColor,
            shadowElevation = if (isActive) 8.dp else 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = stepNumber.toString(),
                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StepConnector(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val width by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        label = "connectorWidth"
    )

    Box(
        modifier = modifier
            .height(2.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(1.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(width)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
        )
    }
}

@Composable
fun Step1_AgeFrequency(viewModel: CompleteProfileViewModel) {
    val age by viewModel.age.collectAsState()
    val travelFrequency by viewModel.travelFrequency.collectAsState()
    var ageInput by remember { mutableStateOf(age?.toString() ?: "") }
    
    // Synchroniser ageInput avec le state du viewModel
    LaunchedEffect(age) {
        ageInput = age?.toString() ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StepTitle(
            icon = Icons.Default.Person,
            title = "Informations de base",
            subtitle = "Votre âge et fréquence de voyage"
        )

        // Âge
        OutlinedTextField(
            value = ageInput,
            onValueChange = {
                ageInput = it
                it.toIntOrNull()?.let { ageValue ->
                    if (ageValue in 18..100) {
                        viewModel.setAge(ageValue)
                    }
                }
            },
            label = { Text("Votre âge") },
            leadingIcon = {
                Icon(Icons.Default.Cake, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            isError = ageInput.toIntOrNull()?.let { it !in 18..100 } == true
        )

        if (ageInput.toIntOrNull()?.let { it !in 18..100 } == true) {
            Text(
                "L'âge doit être entre 18 et 100 ans",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        // Fréquence de voyage
        Text(
            "À quelle fréquence voyagez-vous ?",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        SelectionCard(
            icon = Icons.Default.FlightTakeoff,
            title = "Rare",
            subtitle = "1-2 fois par an",
            isSelected = travelFrequency == TravelFrequency.RARE,
            onClick = { viewModel.setTravelFrequency(TravelFrequency.RARE) }
        )

        SelectionCard(
            icon = Icons.Default.Luggage,
            title = "Occasionnel",
            subtitle = "3-5 fois par an",
            isSelected = travelFrequency == TravelFrequency.OCCASIONAL,
            onClick = { viewModel.setTravelFrequency(TravelFrequency.OCCASIONAL) }
        )

        SelectionCard(
            icon = Icons.Default.CardTravel,
            title = "Fréquent",
            subtitle = "6+ fois par an",
            isSelected = travelFrequency == TravelFrequency.FREQUENT,
            onClick = { viewModel.setTravelFrequency(TravelFrequency.FREQUENT) }
        )
    }
}

@Composable
fun Step2_Destinations(viewModel: CompleteProfileViewModel) {
    val preferredDestinations by viewModel.preferredDestinations.collectAsState()
    var newDestination by remember { mutableStateOf("") }

    val popularDestinations = listOf(
        "Europe", "Asie", "Amérique du Nord", "Afrique",
        "Amérique du Sud", "Océanie", "Moyen-Orient", "Caraïbes"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StepTitle(
            icon = Icons.Default.Public,
            title = "Destinations préférées",
            subtitle = "Où aimez-vous voyager ?"
        )

        // Input pour nouvelle destination
        OutlinedTextField(
            value = newDestination,
            onValueChange = { newDestination = it },
            label = { Text("Ajouter une destination") },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            },
            trailingIcon = {
                if (newDestination.isNotBlank()) {
                    IconButton(
                        onClick = {
                            viewModel.addDestination(newDestination.trim())
                            newDestination = ""
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        // Destinations populaires
        Text(
            "Destinations populaires",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(popularDestinations) { destination ->
                FilterChip(
                    selected = preferredDestinations.contains(destination),
                    onClick = {
                        if (preferredDestinations.contains(destination)) {
                            viewModel.removeDestination(destination)
                        } else {
                            viewModel.addDestination(destination)
                        }
                    },
                    label = { Text(destination) },
                    leadingIcon = if (preferredDestinations.contains(destination)) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }

        // Destinations sélectionnées
        if (preferredDestinations.isNotEmpty()) {
            Text(
                "Vos destinations (${preferredDestinations.size})",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            preferredDestinations.forEach { destination ->
                DestinationChip(
                    destination = destination,
                    onRemove = { viewModel.removeDestination(destination) }
                )
            }
        }
    }
}

@Composable
fun Step3_BudgetPurpose(viewModel: CompleteProfileViewModel) {
    val budgetRange by viewModel.budgetRange.collectAsState()
    val travelPurpose by viewModel.travelPurpose.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StepTitle(
            icon = Icons.Default.AttachMoney,
            title = "Budget et objectif",
            subtitle = "Votre style de voyage"
        )

        // Budget
        Text(
            "Quelle est votre gamme de budget ?",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        SelectionCard(
            icon = Icons.Default.Savings,
            title = "Économique",
            subtitle = "Voyages à petit budget",
            isSelected = budgetRange == BudgetRange.BUDGET,
            onClick = { viewModel.setBudgetRange(BudgetRange.BUDGET) }
        )

        SelectionCard(
            icon = Icons.Default.AccountBalance,
            title = "Moyen",
            subtitle = "Confort à prix raisonnable",
            isSelected = budgetRange == BudgetRange.MEDIUM,
            onClick = { viewModel.setBudgetRange(BudgetRange.MEDIUM) }
        )

        SelectionCard(
            icon = Icons.Default.Diamond,
            title = "Premium",
            subtitle = "Luxe et confort maximum",
            isSelected = budgetRange == BudgetRange.PREMIUM,
            onClick = { viewModel.setBudgetRange(BudgetRange.PREMIUM) }
        )

        // Objectif
        Text(
            "Quel est l'objectif principal de vos voyages ?",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        SelectionCard(
            icon = Icons.Default.BeachAccess,
            title = "Loisirs",
            subtitle = "Vacances et détente",
            isSelected = travelPurpose == TravelPurpose.LEISURE,
            onClick = { viewModel.setTravelPurpose(TravelPurpose.LEISURE) }
        )

        SelectionCard(
            icon = Icons.Default.BusinessCenter,
            title = "Affaires",
            subtitle = "Voyages professionnels",
            isSelected = travelPurpose == TravelPurpose.BUSINESS,
            onClick = { viewModel.setTravelPurpose(TravelPurpose.BUSINESS) }
        )

        SelectionCard(
            icon = Icons.Default.WorkspacePremium,
            title = "Les deux",
            subtitle = "Loisirs et affaires",
            isSelected = travelPurpose == TravelPurpose.BOTH,
            onClick = { viewModel.setTravelPurpose(TravelPurpose.BOTH) }
        )
    }
}

@Composable
fun Step4_CompanionHealth(viewModel: CompleteProfileViewModel) {
    val companionType by viewModel.companionType.collectAsState()
    val hasHealthConditions by viewModel.hasHealthConditions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StepTitle(
            icon = Icons.Default.Info,
            title = "Dernières informations",
            subtitle = "Presque terminé !"
        )

        // Type de compagnon
        Text(
            "Avec qui voyagez-vous habituellement ?",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        SelectionCard(
            icon = Icons.Default.Person,
            title = "Seul",
            subtitle = "Voyages en solo",
            isSelected = companionType == CompanionType.SOLO,
            onClick = { viewModel.setCompanionType(CompanionType.SOLO) }
        )

        SelectionCard(
            icon = Icons.Default.Favorite,
            title = "En couple",
            subtitle = "Voyages romantiques",
            isSelected = companionType == CompanionType.COUPLE,
            onClick = { viewModel.setCompanionType(CompanionType.COUPLE) }
        )

        SelectionCard(
            icon = Icons.Default.FamilyRestroom,
            title = "En famille",
            subtitle = "Avec enfants",
            isSelected = companionType == CompanionType.FAMILY,
            onClick = { viewModel.setCompanionType(CompanionType.FAMILY) }
        )

        SelectionCard(
            icon = Icons.Default.Groups,
            title = "En groupe",
            subtitle = "Avec amis ou groupe",
            isSelected = companionType == CompanionType.GROUP,
            onClick = { viewModel.setCompanionType(CompanionType.GROUP) }
        )

        // Besoins médicaux
        Text(
            "Avez-vous des besoins médicaux spéciaux ?",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectionCard(
                icon = Icons.Default.Check,
                title = "Oui",
                subtitle = "",
                isSelected = hasHealthConditions == true,
                onClick = { viewModel.setHasHealthConditions(true) },
                modifier = Modifier.weight(1f)
            )

            SelectionCard(
                icon = Icons.Default.Close,
                title = "Non",
                subtitle = "",
                isSelected = hasHealthConditions == false,
                onClick = { viewModel.setHasHealthConditions(false) },
                modifier = Modifier.weight(1f)
            )
        }

        // Message de finalisation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        "C'est presque fini !",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Appuyez sur Terminer pour obtenir vos recommandations personnalisées",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun StepTitle(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            subtitle,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SelectionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun DestinationChip(
    destination: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                destination,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileFooter(
    currentStep: Int,
    canProceed: Boolean,
    isLoading: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = onPreviousClick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Précédent")
                }
            }

            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .weight(if (currentStep > 0) 1f else 1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = canProceed && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (currentStep == 3) "Terminer" else "Suivant",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (currentStep == 3) Icons.Default.Check else Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
