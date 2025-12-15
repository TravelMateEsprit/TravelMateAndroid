package com.travelmate.ui.screens.user

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travelmate.data.models.Pack
import com.travelmate.ui.theme.*
import com.travelmate.viewmodel.UserPacksViewModel
import kotlin.math.abs

/**
 * Tinder-style pack browsing screen - swipe left/right to see packs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacksBrowseScreen(
    onNavigateToPackDetails: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToConversations: () -> Unit,
    viewModel: UserPacksViewModel = hiltViewModel()
) {
    val packs by viewModel.availablePacks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var currentIndex by remember { mutableStateOf(0) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        viewModel.loadAvailablePacks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Découvrir les packs",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { /* Back navigation handled by parent */ }) {
                        Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    // Chat icon with badge - Navigate to conversations list
                    IconButton(onClick = onNavigateToConversations) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = ColorError,
                                    contentColor = Color.White
                                ) {
                                    Text("3", fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ChatBubble, "Messages", tint = Color.White)
                        }
                    }
                    // Favorites icon
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(Icons.Default.Favorite, "Favoris", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ColorBackground)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
                packs.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = ColorTextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                "Aucun pack disponible",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            Text(
                                "Revenez plus tard",
                                fontSize = 14.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }
                }
                currentIndex >= packs.size -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ColorSuccess,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                "Vous avez vu tous les packs!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorTextPrimary
                            )
                            Text(
                                "Consultez vos favoris ou revenez plus tard",
                                fontSize = 14.sp,
                                color = ColorTextSecondary
                            )
                            Button(
                                onClick = { currentIndex = 0 },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                            ) {
                                Text("Revoir")
                            }
                        }
                    }
                }
                else -> {
                    // Show current and next pack for smooth transitions
                    val currentPack = packs[currentIndex]
                    val nextPack = if (currentIndex + 1 < packs.size) packs[currentIndex + 1] else null

                    // Next pack (behind)
                    nextPack?.let { pack ->
                        PackCard(
                            pack = pack,
                            offsetX = 0f,
                            offsetY = 0f,
                            rotation = 0f,
                            scale = 0.95f,
                            alpha = 0.8f,
                            zIndex = 0f,
                            onSwipe = { /* Do nothing for next pack */ },
                            onTap = { /* Do nothing */ },
                            onOffsetChange = { _, _ -> /* Do nothing */ },
                            viewModel = viewModel
                        )
                    }

                    // Current pack (on top)
                    PackCard(
                        pack = currentPack,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        rotation = (offsetX / 10f).coerceIn(-15f, 15f),
                        scale = if (abs(offsetX) > 100) 0.9f else 1f,
                        alpha = if (abs(offsetX) > 200) 0.7f else 1f,
                        zIndex = 1f,
                        onSwipe = { direction ->
                            if (direction == SwipeDirection.LEFT) {
                                // Swipe left = next pack
                                currentIndex++
                            } else if (direction == SwipeDirection.RIGHT) {
                                // Swipe right = see details
                                onNavigateToPackDetails(currentPack.id)
                            }
                            offsetX = 0f
                            offsetY = 0f
                        },
                        onTap = {
                            onNavigateToPackDetails(currentPack.id)
                        },
                        onOffsetChange = { newX, newY ->
                            offsetX = newX
                            offsetY = newY
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun PackCard(
    pack: Pack,
    offsetX: Float,
    offsetY: Float,
    rotation: Float,
    scale: Float,
    alpha: Float,
    zIndex: Float,
    onSwipe: (SwipeDirection) -> Unit,
    onTap: () -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    viewModel: UserPacksViewModel
) {
    var isFavorite by remember { mutableStateOf(false) }
    
    LaunchedEffect(pack.id) {
        isFavorite = viewModel.isFavorite(pack.id)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .zIndex(zIndex)
            .offset(x = offsetX.dp, y = offsetY.dp)
            .rotate(rotation)
            .scale(scale)
            .alpha(alpha)
            .pointerInput(pack.id) {
                var totalDragX = 0f
                var totalDragY = 0f

                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                        onOffsetChange(totalDragX, totalDragY)
                    },
                    onDragEnd = {
                        val threshold = 200f
                        when {
                            abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > threshold -> {
                                if (totalDragX > 0) {
                                    onSwipe(SwipeDirection.RIGHT)
                                } else {
                                    onSwipe(SwipeDirection.LEFT)
                                }
                            }
                            abs(totalDragY) > threshold && totalDragY < 0 -> {
                                // Swipe up = add to favorites
                                if (!isFavorite) {
                                    viewModel.addToFavorites(pack.id)
                                    isFavorite = true
                                }
                                onOffsetChange(0f, 0f)
                            }
                            else -> {
                                // Snap back
                                onOffsetChange(0f, 0f)
                            }
                        }
                    }
                )
            }
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box {
                // Background image or gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ColorPrimary.copy(alpha = 0.8f),
                                    ColorPrimary.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // Pack image if available
                if (pack.images.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(pack.images.first())
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top bar with favorite icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                if (isFavorite) {
                                    viewModel.removeFromFavorites(pack.id)
                                } else {
                                    viewModel.addToFavorites(pack.id)
                                }
                                isFavorite = !isFavorite
                            }
                        ) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favoris",
                                tint = if (isFavorite) ColorError else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Pack details
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            pack.titre,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                pack.destination.orEmpty(),
                                fontSize = 18.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "${pack.dateDebut} - ${pack.dateFin}",
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                color = ColorSuccess.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "${pack.prix.toInt()} DT",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Swipe hints
                if (abs(offsetX) < 50f && abs(offsetY) < 50f) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(0.3f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "← Swipe pour voir le suivant",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "→ Swipe pour voir les détails",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "↑ Swipe pour ajouter aux favoris",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

enum class SwipeDirection {
    LEFT,
    RIGHT,
    UP,
    DOWN
}
