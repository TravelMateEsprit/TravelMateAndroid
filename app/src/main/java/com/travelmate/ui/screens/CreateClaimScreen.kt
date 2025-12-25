package com.travelmate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.travelmate.data.models.InsuranceRequest
import com.travelmate.data.models.RequestStatus
import com.travelmate.data.models.ClaimCategory
import com.travelmate.ui.theme.ColorSuccess
import com.travelmate.ui.user.requests.MyInsuranceRequestsViewModel
import com.travelmate.ui.user.requests.MyRequestsState
import com.travelmate.utils.Constants
import com.travelmate.viewmodel.ClaimViewModel
import com.travelmate.viewmodel.InsurancesUserViewModel

data class CategoryOption(
    val category: ClaimCategory,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClaimScreen(
    navController: NavController,
    insuranceId: String? = null,
    viewModel: ClaimViewModel = hiltViewModel(),
    requestViewModel: MyInsuranceRequestsViewModel = hiltViewModel(),
    insuranceViewModel: InsurancesUserViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedInsuranceRequestId by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<ClaimCategory?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    
    val createSuccess by viewModel.createClaimSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val requestState by requestViewModel.state.collectAsState()
    val mySubscriptions by insuranceViewModel.mySubscriptions.collectAsState()
    
    val allRequests = remember(requestState) {
        if (requestState is MyRequestsState.Success) {
            (requestState as MyRequestsState.Success).requests
        } else {
            emptyList()
        }
    }
    
    val activeInsuranceRequests = remember(allRequests, mySubscriptions) {
        val subscribedInsuranceIds = mySubscriptions.map { it._id }.toSet()
        allRequests.filter { 
            it.status == RequestStatus.APPROVED && 
            subscribedInsuranceIds.contains(it.insuranceId)
        }
    }
    
    LaunchedEffect(insuranceId, activeInsuranceRequests) {
        if (insuranceId != null && selectedInsuranceRequestId == null && activeInsuranceRequests.isNotEmpty()) {
            activeInsuranceRequests.find { it.insuranceId == insuranceId }?.let { request ->
                selectedInsuranceRequestId = request.id
            }
        }
    }
    
    LaunchedEffect(Unit) {
        requestViewModel.loadRequests()
        insuranceViewModel.loadMySubscriptions()
    }
    
    LaunchedEffect(createSuccess) {
        if (createSuccess) {
            navController.navigate(Constants.Routes.MY_CLAIMS) {
                popUpTo(Constants.Routes.MY_CLAIMS) { inclusive = true }
            }
            viewModel.resetCreateClaimSuccess()
        }
    }
    
    val categories = remember {
        listOf(
            CategoryOption(
                ClaimCategory.REFUND,
                "Remboursement",
                Icons.Outlined.Payments,
                Color(0xFF4CAF50),
                "Demande de remboursement de frais"
            ),
            CategoryOption(
                ClaimCategory.BOOKING,
                "Réservation",
                Icons.Outlined.EventAvailable,
                Color(0xFFF44336),
                "Problème de réservation ou annulation"
            ),
            CategoryOption(
                ClaimCategory.PAYMENT,
                "Paiement",
                Icons.Outlined.CreditCard,
                Color(0xFFE91E63),
                "Problème de paiement"
            ),
            CategoryOption(
                ClaimCategory.COVERAGE,
                "Couverture",
                Icons.Outlined.Shield,
                Color(0xFF9C27B0),
                "Question sur la couverture d'assurance"
            ),
            CategoryOption(
                ClaimCategory.CLAIM_PROCESS,
                "Procédure",
                Icons.Outlined.Assignment,
                Color(0xFF2196F3),
                "Question sur la procédure de réclamation"
            ),
            CategoryOption(
                ClaimCategory.TECHNICAL,
                "Technique",
                Icons.Outlined.Build,
                Color(0xFFFF9800),
                "Problème technique avec la plateforme"
            ),
            CategoryOption(
                ClaimCategory.OTHER,
                "Autre",
                Icons.Outlined.MoreHoriz,
                Color(0xFF607D8B),
                "Autre type de réclamation"
            )
        )
    }
    
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = colorScheme.primary
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nouveau Ticket",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimary
                            )
                            Text(
                                text = "Décrivez votre problème",
                                fontSize = 13.sp,
                                color = colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                    
                    // Stepper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StepIndicator(
                            stepNumber = 1,
                            label = "Catégorie",
                            isActive = currentStep == 1,
                            isCompleted = currentStep > 1,
                            colorScheme = colorScheme
                        )
                        StepConnector(isCompleted = currentStep > 1, colorScheme = colorScheme)
                        StepIndicator(
                            stepNumber = 2,
                            label = "Assurance",
                            isActive = currentStep == 2,
                            isCompleted = currentStep > 2,
                            colorScheme = colorScheme
                        )
                        StepConnector(isCompleted = currentStep > 2, colorScheme = colorScheme)
                        StepIndicator(
                            stepNumber = 3,
                            label = "Détails",
                            isActive = currentStep == 3,
                            isCompleted = false,
                            colorScheme = colorScheme
                        )
                    }
                }
            }
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        slideInHorizontally { it } togetherWith 
                        slideOutHorizontally { -it }
                    },
                    label = "step_animation"
                ) { step ->
                    when (step) {
                        1 -> CategorySelectionStep(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { 
                                selectedCategory = it
                                currentStep = 2
                            },
                            colorScheme = colorScheme
                        )
                        
                        2 -> InsuranceSelectionStep(
                            activeRequests = activeInsuranceRequests,
                            selectedRequestId = selectedInsuranceRequestId,
                            onRequestSelected = {
                                selectedInsuranceRequestId = it
                                currentStep = 3
                            },
                            onBack = { currentStep = 1 },
                            colorScheme = colorScheme
                        )
                        
                        3 -> DetailsStep(
                            subject = subject,
                            description = description,
                            onSubjectChange = { if (it.length <= 200) subject = it },
                            onDescriptionChange = { if (it.length <= 2000) description = it },
                            selectedCategory = selectedCategory,
                            selectedRequestId = selectedInsuranceRequestId,
                            isLoading = isLoading,
                            error = error,
                            onSubmit = {
                                if (selectedInsuranceRequestId != null && selectedCategory != null) {
                                    viewModel.createClaim(
                                        insuranceRequestId = selectedInsuranceRequestId!!,
                                        subject = subject.ifBlank { selectedCategory!!.value },
                                        description = description,
                                        category = selectedCategory!!.value
                                    )
                                }
                            },
                            onBack = { currentStep = 2 },
                            colorScheme = colorScheme
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun StepIndicator(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = when {
                isCompleted -> ColorSuccess
                isActive -> colorScheme.onPrimary
                else -> colorScheme.onPrimary.copy(alpha = 0.3f)
            },
            modifier = Modifier.size(40.dp)
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
                        text = "$stepNumber",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isActive) colorScheme.primary else colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = colorScheme.onPrimary.copy(alpha = if (isActive) 1f else 0.7f)
        )
    }
}

@Composable
fun RowScope.StepConnector(
    isCompleted: Boolean,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .align(Alignment.CenterVertically)
            .padding(top = 20.dp)
            .background(
                if (isCompleted) ColorSuccess else colorScheme.onPrimary.copy(alpha = 0.3f)
            )
    )
}

@Composable
fun CategorySelectionStep(
    categories: List<CategoryOption>,
    selectedCategory: ClaimCategory?,
    onCategorySelected: (ClaimCategory) -> Unit,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Column {
        Text(
            text = "Quelle est la nature de votre réclamation ?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sélectionnez la catégorie qui correspond le mieux à votre problème",
            fontSize = 14.sp,
            color = colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        categories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowCategories.forEach { category ->
                    CategoryCard(
                        category = category,
                        isSelected = selectedCategory == category.category,
                        onClick = { onCategorySelected(category.category) },
                        modifier = Modifier.weight(1f),
                        colorScheme = colorScheme
                    )
                }
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) category.color.copy(alpha = 0.15f) else colorScheme.surface
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, category.color)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = category.color.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = category.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) category.color else colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun InsuranceSelectionStep(
    activeRequests: List<InsuranceRequest>,
    selectedRequestId: String?,
    onRequestSelected: (String) -> Unit,
    onBack: () -> Unit,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Text(
                text = "Sélectionnez votre assurance",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (activeRequests.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aucune assurance active",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vous devez avoir une assurance approuvée pour créer un ticket",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            activeRequests.forEach { request ->
                InsuranceRequestCard(
                    request = request,
                    isSelected = selectedRequestId == request.id,
                    onClick = { onRequestSelected(request.id) },
                    colorScheme = colorScheme
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun InsuranceRequestCard(
    request: InsuranceRequest,
    isSelected: Boolean,
    onClick: () -> Unit,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.3f) else colorScheme.surface
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, colorScheme.primary)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.travelerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.destination,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Départ: ${request.departureDate}",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun DetailsStep(
    subject: String,
    description: String,
    onSubjectChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    selectedCategory: ClaimCategory?,
    selectedRequestId: String?,
    isLoading: Boolean,
    error: String?,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Text(
                text = "Décrivez votre problème",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sujet
        Text(
            text = "Titre du ticket",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = subject,
            onValueChange = onSubjectChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ex: Problème de remboursement médical") },
            supportingText = { Text("${subject.length}/200 caractères") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline,
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface
            )
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Description
        Text(
            text = "Description détaillée",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            placeholder = { 
                Text(
                    "Décrivez en détail votre problème, les circonstances, " +
                    "les dates importantes, et toute information pertinente..."
                ) 
            },
            supportingText = { Text("${description.length}/2000 caractères") },
            maxLines = 8,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline,
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Conseil",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Plus votre description est détaillée, plus nous pourrons vous répondre rapidement et efficacement.",
                        fontSize = 12.sp,
                        color = colorScheme.onTertiaryContainer,
                        lineHeight = 16.sp
                    )
                }
            }
        }
        
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = colorScheme.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Submit button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && subject.isNotBlank() && description.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Envoi en cours...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Icon(Icons.Outlined.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Créer le ticket", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
