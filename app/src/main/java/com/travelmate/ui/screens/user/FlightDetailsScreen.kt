package com.travelmate.ui.screens.user

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelmate.data.models.Airport
import com.travelmate.data.models.FlightOffer
import com.travelmate.data.models.FlightSegment
import com.travelmate.data.models.SegmentDetails
import com.travelmate.ui.theme.*
import com.travelmate.utils.PrintHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailsScreen(
    flightOffer: FlightOffer,
    onNavigateBack: () -> Unit,
    onBookFlight: (FlightOffer) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Détails du vol",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { PrintHelper.printFlightDetails(context, flightOffer) }
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Imprimer",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { PrintHelper.printFlightDetails(context, flightOffer) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(ColorPrimary, ColorSecondary),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    null,
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Exporter en PDF",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .background(ColorBackground)
        ) {
            // Header Card with Airline Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ColorPrimary, ColorSecondary),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Icon(
                                    Icons.Default.Flight,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(10.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = flightOffer.getAirlineName().ifEmpty { "Compagnie aérienne" },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                                flightOffer.flightNumber?.let {
                                    Text(
                                        text = "Vol $it",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = flightOffer.getFormattedPrice(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = "par personne",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Flight Route Header
                FlightRouteHeader(
                    from = flightOffer.getFromAirport(),
                    to = flightOffer.getToAirport(),
                    type = flightOffer.getTypeValue()
                )
                
                // Duration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        ColorAccent.copy(alpha = 0.1f),
                                        ColorAccent.copy(alpha = 0.05f)
                                    ),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp),
                                    color = Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(ColorAccent.copy(alpha = 0.8f), ColorAccent.copy(alpha = 0.6f)),
                                                    center = androidx.compose.ui.geometry.Offset(24f, 24f),
                                                    radius = 24f
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Durée totale",
                                        fontSize = 14.sp,
                                        color = ColorTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = flightOffer.duration ?: flightOffer.getDepartureSegment()?.getDurationValue() ?: "N/A",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorAccent
                                    )
                                }
                            }
                            if (flightOffer.getReturnSegment() != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ColorPrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "Aller-retour",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Divider(color = ColorTextSecondary.copy(alpha = 0.2f))
                
                // Departure Flight Details
                flightOffer.getDepartureSegment()?.let { departureSegment ->
                    FlightSegmentDetailsCard(
                        segment = departureSegment,
                        from = flightOffer.getFromAirport(),
                        to = flightOffer.getToAirport(),
                        title = "Vol aller",
                        isDirect = departureSegment.isDirect()
                    )
                }
                
                // Return Flight Details (if round trip)
                flightOffer.getReturnSegment()?.let { returnSegment ->
                    Spacer(modifier = Modifier.height(8.dp))
                    FlightSegmentDetailsCard(
                        segment = returnSegment,
                        from = flightOffer.getToAirport(),
                        to = flightOffer.getFromAirport(),
                        title = "Vol retour",
                        isDirect = returnSegment.isDirect()
                    )
                }
                
                Divider(color = ColorTextSecondary.copy(alpha = 0.2f))
                
                // Dates Information
                DatesInfoSection(flightOffer = flightOffer)
                
                Divider(color = ColorTextSecondary.copy(alpha = 0.2f))
                
                // Flight Information
                FlightInfoSection(flightOffer = flightOffer)
                
                Divider(color = ColorTextSecondary.copy(alpha = 0.2f))
                
                // Airport Details
                AirportDetailsSection(flightOffer = flightOffer)
                
                Divider(color = ColorTextSecondary.copy(alpha = 0.2f))
                
                // Price Breakdown
                PriceBreakdownCard(flightOffer = flightOffer)
            }
        }
    }
}

@Composable
fun FlightRouteHeader(
    from: Airport,
    to: Airport,
    type: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ColorPrimary.copy(alpha = 0.08f),
                            ColorSecondary.copy(alpha = 0.05f),
                            ColorPrimary.copy(alpha = 0.08f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 0f)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From Airport
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ColorPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = from.code,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                        Text(
                            text = from.name.ifEmpty { from.code },
                            fontSize = 14.sp,
                            color = ColorTextSecondary,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        from.city?.let {
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = ColorTextSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    
                    // Flight Icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(ColorPrimary, ColorSecondary),
                                            center = androidx.compose.ui.geometry.Offset(24f, 24f),
                                            radius = 24f
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Flight,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ColorAccent.copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = when (type) {
                                    "aller-retour" -> "AR"
                                    "multi-destin" -> "MD"
                                    else -> "AS"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    // To Airport
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ColorSecondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = to.code,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorSecondary,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                        Text(
                            text = to.name.ifEmpty { to.code },
                            fontSize = 14.sp,
                            color = ColorTextSecondary,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            maxLines = 2
                        )
                        to.city?.let {
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = ColorTextSecondary.copy(alpha = 0.7f),
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlightSegmentDetailsCard(
    segment: FlightSegment,
    from: Airport,
    to: Airport,
    title: String,
    isDirect: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            ColorPrimary.copy(alpha = 0.08f),
                            ColorSecondary.copy(alpha = 0.05f),
                            Color.White
                        )
                    )
                )
        ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ColorPrimary.copy(alpha = 0.8f), ColorPrimary.copy(alpha = 0.6f)),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(200f, 0f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isDirect) 
                                        listOf(ColorSuccess.copy(alpha = 0.9f), ColorSuccess.copy(alpha = 0.7f))
                                    else 
                                        listOf(ColorWarning.copy(alpha = 0.9f), ColorWarning.copy(alpha = 0.7f)),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(100f, 0f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isDirect) "Direct" else "${segment.getStops()} escale(s)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
   
            // Flight Number
            segment.flightNumber?.let { flightNum ->
                Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = ColorTextSecondary
                    )
                    Text(
                        text = "Vol: $flightNum",
                        fontSize = 12.sp,
                        color = ColorTextSecondary
                    )
                }
            }
            }
        }
    }
}

@Composable
fun DatesInfoSection(flightOffer: FlightOffer) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dates de voyage",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorPrimary
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                ColorAccent.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                flightOffer.getDepartureDate()?.let { date ->
                    InfoRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date de départ",
                        value = date
                    )
                }
                
                flightOffer.getReturnDate()?.let { date ->
                    Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                    InfoRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date de retour",
                        value = date
                    )
                }
                
                flightOffer.duration?.let { duration ->
                    Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                    InfoRow(
                        icon = Icons.Default.Schedule,
                        label = "Durée totale",
                        value = duration
                    )
                }
            }
            }
        }
    }
}

@Composable
fun FlightInfoSection(flightOffer: FlightOffer) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Informations du vol",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorPrimary
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                ColorPrimary.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    icon = Icons.Default.Flight,
                    label = "Compagnie aérienne",
                    value = flightOffer.getAirlineName().ifEmpty { "Non spécifiée" }
                )
                
                flightOffer.flightNumber?.let {
                    Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                    InfoRow(
                        icon = Icons.Default.Info,
                        label = "Numéro de vol",
                        value = it
                    )
                }
                
                Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                InfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Type de vol",
                    value = when (flightOffer.getTypeValue()) {
                        "aller-retour" -> "Aller-retour"
                        "multi-destin" -> "Multi-destinations"
                        else -> "Aller simple"
                    }
                )
                
                if (flightOffer.availableSeats != null) {
                    Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                    InfoRow(
                        icon = Icons.Default.EventSeat,
                        label = "Places disponibles",
                        value = "${flightOffer.availableSeats} places"
                    )
                }
                
                flightOffer.direct?.let { isDirect ->
                    Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                    InfoRow(
                        icon = Icons.Default.Flight,
                        label = "Type de trajet",
                        value = if (isDirect) "Vol direct" else "Avec escale(s)"
                    )
                }
                
                flightOffer.stops?.let { stops ->
                    if (stops > 0) {
                        Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                        InfoRow(
                            icon = Icons.Default.Info,
                            label = "Nombre d'escales",
                            value = "$stops escale(s)"
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
fun AirportDetailsSection(flightOffer: FlightOffer) {
    val fromAirport = flightOffer.getFromAirport()
    val toAirport = flightOffer.getToAirport()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Détails des aéroports",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorPrimary
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                ColorSecondary.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Departure Airport
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FlightTakeoff,
                            null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Aéroport de départ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${fromAirport.code.ifEmpty { "N/A" }} - ${fromAirport.name.ifEmpty { "Aéroport" }}",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                    fromAirport.city?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = ColorTextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    fromAirport.country?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = ColorTextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                
                // Arrival Airport
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FlightLand,
                            null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Aéroport d'arrivée",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${toAirport.code.ifEmpty { "N/A" }} - ${toAirport.name.ifEmpty { "Aéroport" }}",
                        fontSize = 14.sp,
                        color = ColorTextSecondary
                    )
                    toAirport.city?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = ColorTextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    toAirport.country?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = ColorTextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(20.dp),
                tint = ColorPrimary
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = ColorTextSecondary
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ColorTextPrimary
        )
    }
}

@Composable
fun PriceBreakdownCard(flightOffer: FlightOffer) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Détail des prix",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorPrimary
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                ColorAccent.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PriceRow("Prix du vol", flightOffer.getFormattedPrice())
                Divider(color = ColorTextSecondary.copy(alpha = 0.1f))
                PriceRow(
                    "Total",
                    flightOffer.getFormattedPrice(),
                    isTotal = true
                )
            }
            }
        }
    }
}

@Composable
fun PriceRow(
    label: String,
    price: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isTotal) 16.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) ColorTextPrimary else ColorTextSecondary
        )
        Text(
            text = price,
            fontSize = if (isTotal) 20.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isTotal) ColorPrimary else ColorTextPrimary
        )
    }
}

