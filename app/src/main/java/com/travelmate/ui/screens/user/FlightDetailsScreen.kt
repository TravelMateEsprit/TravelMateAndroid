package com.travelmate.ui.screens.user

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.travelmate.data.models.Airport
import com.travelmate.data.models.FlightOffer
import com.travelmate.data.models.FlightSegment
import com.travelmate.ui.theme.*
import com.travelmate.utils.CityCoordinates
import com.travelmate.utils.PrintHelper
import com.travelmate.viewmodel.OffersViewModel
import java.net.URLEncoder
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailsScreen(
        viewModel: OffersViewModel,
        onNavigateBack: () -> Unit,
        onBookFlight: (FlightOffer) -> Unit = {}
) {
    // Get selected offer from ViewModel
    val selectedOffer by viewModel.selectedOffer.collectAsState()
    val offers by viewModel.offers.collectAsState()

    // Handle loading/error state
    if (selectedOffer == null) {
        // Show loading or error state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(text = "Chargement...", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val flightOffer = selectedOffer!!
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showConfirmation by remember { mutableStateOf(false) }

    /** Map airline code to airline website URL */
    fun getAirlineWebsite(airlineCode: String): String? {
        return when (airlineCode.uppercase()) {
            "AT", "ROYAL AIR MAROC" -> "https://www.royalairmaroc.com"
            "TU", "TUNISAIR" -> "https://www.tunisair.com"
            "AF", "AIR FRANCE" -> "https://www.airfrance.com"
            "TK", "TURKISH AIRLINES" -> "https://www.turkishairlines.com"
            "LH", "LUFTHANSA" -> "https://www.lufthansa.com"
            "EK", "EMIRATES" -> "https://www.emirates.com"
            "AZ", "ITA AIRWAYS", "ALITALIA" -> "https://www.ita-airways.com"
            "QR", "QATAR AIRWAYS" -> "https://www.qatarairways.com"
            "BA", "BRITISH AIRWAYS" -> "https://www.britishairways.com"
            "KL", "KLM" -> "https://www.klm.com"
            "MS", "EGYPTAIR" -> "https://www.egyptair.com"
            else -> null
        }
    }

    /**
     * Build Google Flights URL for booking Tries multiple sources to extract airport codes and
     * dates
     */
    fun buildGoogleFlightsUrl(): String {
        // Try to get airport codes from multiple sources
        var originCode = flightOffer.getFromAirport().code
        var destinationCode = flightOffer.getToAirport().code

        // If codes are empty, try to get them from departure segment
        if (originCode.isEmpty() || destinationCode.isEmpty()) {
            val departureSegment = flightOffer.getDepartureSegment()
            if (departureSegment != null) {
                val depDetails = departureSegment.getDepartureDetails()
                val arrDetails = departureSegment.getArrivalDetails()

                if (originCode.isEmpty()) {
                    originCode = depDetails.getAirport().code
                }
                if (destinationCode.isEmpty()) {
                    destinationCode = arrDetails.getAirport().code
                }
            }
        }

        // Get dates from multiple sources
        var departureDate = flightOffer.getDepartureDate()
        var returnDate = flightOffer.getReturnDate()

        // Try to get date from departure segment if not available
        if (departureDate.isNullOrEmpty()) {
            val departureSegment = flightOffer.getDepartureSegment()
            departureSegment?.getDepartureDetails()?.date?.let { departureDate = it }
        }

        // Build Google Flights URL
        // Format: https://www.google.com/travel/flights?q=Flights%20from%20TUN%20to%20FCO
        val baseUrl = "https://www.google.com/travel/flights"
        val query = buildString {
            if (originCode.isNotEmpty() && destinationCode.isNotEmpty()) {
                append("Flights from $originCode to $destinationCode")
                val dep = departureDate
                dep?.let {
                    // Format date if needed (remove time if present)
                    val dateOnly = it.split("T")[0].split(" ")[0]
                    append(" on $dateOnly")
                }
                val ret = returnDate
                ret?.let {
                    val returnDateOnly = it.split("T")[0].split(" ")[0]
                    append(" returning $returnDateOnly")
                }
            } else {
                // Fallback: generic flight search
                append("Flights")
            }
        }

        return try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            "$baseUrl?q=$encodedQuery"
        } catch (e: Exception) {
            // Fallback to generic Google Flights URL
            "https://www.google.com/travel/flights"
        }
    }

    /** Build booking URL - tries airline website first, falls back to Google Flights */
    fun buildBookingUrl(): String {
        // Try to get airline code/name
        val airlineName = flightOffer.getAirlineName()

        // Try to get airline website first
        if (airlineName.isNotEmpty()) {
            val airlineUrl = getAirlineWebsite(airlineName)
            if (airlineUrl != null) {
                return airlineUrl
            }
        }

        // Also check departure segment for airline code
        val departureSegment = flightOffer.getDepartureSegment()
        departureSegment?.airline?.let { segmentAirline ->
            val airlineUrl = getAirlineWebsite(segmentAirline)
            if (airlineUrl != null) {
                return airlineUrl
            }
        }

        // Fallback to Google Flights if airline not found
        return buildGoogleFlightsUrl()
    }

    /** Validate and clean URL */
    fun validateAndCleanUrl(url: String): String? {
        // Remove whitespace and line breaks
        val cleaned = url.trim().replace("\n", "").replace("\r", "")

        // Ensure URL starts with https://
        return when {
            cleaned.startsWith("https://") -> cleaned
            cleaned.startsWith("http://") -> cleaned.replace("http://", "https://")
            cleaned.startsWith("//") -> "https:$cleaned"
            else -> "https://$cleaned"
        }
    }

    /**
     * Open booking URL in external browser using explicit Intent Always opens a URL, even if some
     * flight data is missing
     */
    fun openBookingUrl() {
        val rawUrl = buildBookingUrl()
        val url = validateAndCleanUrl(rawUrl)

        if (url == null || url.isEmpty()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                        message = "URL invalide",
                        duration = SnackbarDuration.Short
                )
            }
            return
        }

        try {
            // Use explicit Intent to open in external browser
            val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        // Ensure it opens in external browser, not WebView
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setPackage(null) // Let user choose browser
                    }

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                // Show snackbar message
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                            message = "Ouverture du site de la compagnie aérienne…",
                            duration = SnackbarDuration.Short
                    )
                }
            } else {
                // Fallback to uriHandler if Intent fails
                uriHandler.openUri(url)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                            message = "Redirection vers le site de réservation...",
                            duration = SnackbarDuration.Short
                    )
                }
            }
        } catch (e: Exception) {
            // Show error message if URL cannot be opened
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                        message = "Impossible d'ouvrir le site de réservation: ${e.message}",
                        duration = SnackbarDuration.Short
                )
            }
        }
    }

    fun shareFlightDetails(context: Context, flight: FlightOffer) {
        val airline = flight.getAirlineName().ifEmpty { "Compagnie non spécifiée" }
        val fromAirport = flight.getFromAirport()
        val toAirport = flight.getToAirport()
        val originLabel = fromAirport.city ?: (fromAirport.name.ifEmpty { fromAirport.code })
        val destinationLabel = toAirport.city ?: (toAirport.name.ifEmpty { toAirport.code })
        val departDate = flight.getDepartureDate() ?: "N/A"
        val priceValue = flight.getPrice().toInt()
        val bookingUrl =
                validateAndCleanUrl(buildBookingUrl()) ?: "https://www.google.com/travel/flights"
        val shareText = buildString {
            appendLine("✈️ Vol trouvé sur TravelMate")
            appendLine()
            appendLine("Compagnie: $airline")
            appendLine("Route: $originLabel → $destinationLabel")
            appendLine("Date départ: $departDate")
            appendLine("Prix: $priceValue TND par personne")
            appendLine()
            appendLine("🔗 Réserver maintenant:")
            appendLine(bookingUrl)
        }
        val sendIntent =
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
        val shareIntent = Intent.createChooser(sendIntent, "Partager le vol via")
        context.startActivity(shareIntent)
    }

    // Confirmation Dialog
    if (showConfirmation) {
        AlertDialog(
                onDismissRequest = { showConfirmation = false },
                icon = {
                    Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ColorSuccess,
                            modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                            text = "Réservation confirmée",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                                text = "Votre vol a été réservé avec succès !",
                                textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                            onClick = {
                                showConfirmation = false
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("OK") }
                },
                shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Text(
                                    "Détails du vol",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Retour",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                    onClick = {
                                        PrintHelper.printFlightDetails(context, flightOffer)
                                    }
                            ) {
                                Icon(
                                        Icons.Default.Print,
                                        contentDescription = "Imprimer",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            IconButton(onClick = { shareFlightDetails(context, flightOffer) }) {
                                Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Partager le vol",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            },
            bottomBar = {
                Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                                onClick = { openBookingUrl() },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent
                                        ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                    modifier =
                                            Modifier.fillMaxSize()
                                                    .background(
                                                            brush =
                                                                    Brush.linearGradient(
                                                                            colors =
                                                                                    listOf(
                                                                                            MaterialTheme.colorScheme.primary,
                                                                                            ColorSecondary
                                                                                    ),
                                                                            start =
                                                                                    androidx.compose
                                                                                            .ui
                                                                                            .geometry
                                                                                            .Offset(
                                                                                                    0f,
                                                                                                    0f
                                                                                            ),
                                                                            end =
                                                                                    androidx.compose
                                                                                            .ui
                                                                                            .geometry
                                                                                            .Offset(
                                                                                                    1000f,
                                                                                                    0f
                                                                                            )
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
                                            Icons.Default.CheckCircle,
                                            null,
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                            "Réserver",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .verticalScroll(scrollState)
                                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Card with Airline Info
            Card(
                    modifier =
                            Modifier.fillMaxWidth().padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                brush =
                                                        Brush.linearGradient(
                                                                colors =
                                                                        listOf(
                                                                                MaterialTheme.colorScheme.primary,
                                                                                ColorSecondary
                                                                        ),
                                                                start =
                                                                        androidx.compose.ui.geometry
                                                                                .Offset(0f, 0f),
                                                                end =
                                                                        androidx.compose.ui.geometry
                                                                                .Offset(1000f, 0f)
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
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                                    modifier = Modifier.size(50.dp)
                            ) {
                                Icon(
                                        Icons.Default.Flight,
                                        null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(30.dp).padding(10.dp)
                                )
                            }
                            Column {
                                Text(
                                        text =
                                                flightOffer.getAirlineName().ifEmpty {
                                                    "Compagnie aérienne"
                                                },
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 18.sp
                                )
                                flightOffer.flightNumber?.let {
                                    Text(
                                            text = "Vol $it",
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                            fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                        text = flightOffer.getFormattedPrice(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 24.sp,
                                        modifier =
                                                Modifier.padding(
                                                        start = 12.dp,
                                                        top = 6.dp,
                                                        end = 12.dp,
                                                        bottom = 6.dp
                                                )
                                )
                            }
                            Text(
                                    text = "par personne",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Main Content
            Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Flight Route Header
                FlightRouteHeader(
                        from = flightOffer.getFromAirport(),
                        to = flightOffer.getToAirport(),
                        type = flightOffer.getTypeValue()
                )

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

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

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                // Dates Information
                DatesInfoSection(flightOffer = flightOffer)

                // Itinéraire
                Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                                text = "Itinéraire",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val originCity =
                                flightOffer.getFromAirport().city
                                        ?: flightOffer.getFromAirport().name.ifEmpty {
                                            flightOffer.getFromAirport().code
                                        }
                        val destinationCity =
                                flightOffer.getToAirport().city
                                        ?: flightOffer.getToAirport().name.ifEmpty {
                                            flightOffer.getToAirport().code
                                        }
                        FlightRouteMap(
                                originCity = originCity,
                                destinationCity = destinationCity,
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                // Flight Information
                FlightInfoSection(flightOffer = flightOffer)

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                // Airport Details
                AirportDetailsSection(flightOffer = flightOffer)

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                // Price Breakdown
                PriceBreakdownCard(flightOffer = flightOffer)
            }
        }
    }
}

@Composable
fun FlightRouteHeader(from: Airport, to: Airport, type: String) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            // Type Badge
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(
                        shape = RoundedCornerShape(20.dp),
                        color =
                                when (type) {
                                    "aller-retour" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else -> ColorAccent.copy(alpha = 0.1f)
                                }
                ) {
                    Text(
                            text =
                                    when (type) {
                                        "aller-retour" -> "Aller-Retour"
                                        "multi-destin" -> "Multi-destinations"
                                        else -> "Aller Simple"
                                    },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color =
                                    when (type) {
                                        "aller-retour" -> MaterialTheme.colorScheme.primary
                                        else -> ColorAccent
                                    },
                            modifier = Modifier.padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Flight Route Display
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // From Airport
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = from.code,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text = from.city ?: from.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                    )
                    from.country?.let {
                        Text(
                                text = it,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                        )
                    }
                }

                // Arrow and Flight Icon
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Dashed Line with Plane
                    Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                        // Horizontal dashed line
                        Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                            val pathEffect =
                                    androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                            floatArrayOf(10f, 10f),
                                            0f
                                    )
                            drawLine(
                                    color = androidx.compose.ui.graphics.Color(0xFF1976D2),
                                    start =
                                            androidx.compose.ui.geometry.Offset(
                                                    0f,
                                                    size.height / 2
                                            ),
                                    end =
                                            androidx.compose.ui.geometry.Offset(
                                                    size.width,
                                                    size.height / 2
                                            ),
                                    strokeWidth = 3f,
                                    pathEffect = pathEffect
                            )
                        }

                        // Flight icon in center
                        Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                        Icons.Default.Flight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // To Airport
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                            text = to.code,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text = to.city ?: to.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            maxLines = 1
                    )
                    to.country?.let {
                        Text(
                                text = it,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.End,
                                maxLines = 1
                        )
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(
                                        brush =
                                                Brush.verticalGradient(
                                                        colors =
                                                                listOf(
                                                                        MaterialTheme.colorScheme.surface,
                                                                        MaterialTheme.colorScheme.primary.copy(
                                                                                alpha = 0.08f
                                                                        ),
                                                                        ColorSecondary.copy(
                                                                                alpha = 0.05f
                                                                        ),
                                                                        MaterialTheme.colorScheme.surface
                                                                )
                                                )
                                )
        ) {
            Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title Row
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.Transparent) {
                        Box(
                                modifier =
                                        Modifier.background(
                                                        brush =
                                                                Brush.linearGradient(
                                                                        colors =
                                                                                listOf(
                                                                                        MaterialTheme.colorScheme.primary
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.8f
                                                                                                ),
                                                                                        MaterialTheme.colorScheme.primary
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.6f
                                                                                                )
                                                                                ),
                                                                        start =
                                                                                androidx.compose.ui
                                                                                        .geometry
                                                                                        .Offset(
                                                                                                0f,
                                                                                                0f
                                                                                        ),
                                                                        end =
                                                                                androidx.compose.ui
                                                                                        .geometry
                                                                                        .Offset(
                                                                                                200f,
                                                                                                0f
                                                                                        )
                                                                ),
                                                        shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp)
                        ) {
                            Text(
                                    text = title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.Transparent) {
                        Box(
                                modifier =
                                        Modifier.background(
                                                        brush =
                                                                Brush.linearGradient(
                                                                        colors =
                                                                                if (isDirect)
                                                                                        listOf(
                                                                                                ColorSuccess
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.9f
                                                                                                        ),
                                                                                                ColorSuccess
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.7f
                                                                                                        )
                                                                                        )
                                                                                else
                                                                                        listOf(
                                                                                                ColorWarning
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.9f
                                                                                                        ),
                                                                                                ColorWarning
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.7f
                                                                                                        )
                                                                                        ),
                                                                        start =
                                                                                androidx.compose.ui
                                                                                        .geometry
                                                                                        .Offset(
                                                                                                0f,
                                                                                                0f
                                                                                        ),
                                                                        end =
                                                                                androidx.compose.ui
                                                                                        .geometry
                                                                                        .Offset(
                                                                                                100f,
                                                                                                0f
                                                                                        )
                                                                ),
                                                        shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(start = 10.dp, top = 4.dp, end = 10.dp, bottom = 4.dp)
                        ) {
                            Text(
                                    text =
                                            if (isDirect) "Direct"
                                            else "${segment.getStops()} escale(s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Duration Row
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
                                modifier = Modifier.size(40.dp),
                                color = ColorAccent.copy(alpha = 0.15f)
                        ) {
                            Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                        Icons.Default.Schedule,
                                        null,
                                        tint = ColorAccent,
                                        modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(text = "Durée", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                    text = segment.getDurationValue() ?: "N/A",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorAccent
                            )
                        }
                    }
                }

                // Flight Number
                segment.flightNumber?.let { flightNum ->
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                                Icons.Default.Info,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = "Vol: $flightNum", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun DatesInfoSection(flightOffer: FlightOffer) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
                text = "Dates de voyage",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
        )

        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(
                                            brush =
                                                    Brush.verticalGradient(
                                                            colors =
                                                                    listOf(
                                                                            MaterialTheme.colorScheme.surface,
                                                                            ColorAccent.copy(
                                                                                    alpha = 0.05f
                                                                            )
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
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        InfoRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Date de retour",
                                value = date
                        )
                    }

                    flightOffer.duration?.let { duration ->
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
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
fun FlightRouteMap(originCity: String, destinationCity: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isMapReady by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp))) {
        if (!hasError) {
            AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        try {
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                isHorizontalMapRepetitionEnabled = false
                                isVerticalMapRepetitionEnabled = false
                                minZoomLevel = 2.0
                                maxZoomLevel = 10.0

                                val originPoint = CityCoordinates.getCoordinates(originCity)
                                val destPoint = CityCoordinates.getCoordinates(destinationCity)

                                if (originPoint != null && destPoint != null) {
                                    // Post to handler to avoid blocking UI thread
                                    post {
                                        try {
                                            val originMarker =
                                                    Marker(this).apply {
                                                        position = originPoint
                                                        title = originCity
                                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                    }
                                            val destMarker =
                                                    Marker(this).apply {
                                                        position = destPoint
                                                        title = destinationCity
                                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                    }
                                            overlays.add(originMarker)
                                            overlays.add(destMarker)

                                            val line =
                                                    Polyline().apply {
                                                        addPoint(originPoint)
                                                        addPoint(destPoint)
                                                        outlinePaint.color =
                                                                android.graphics.Color.parseColor("#1976D2")
                                                        outlinePaint.strokeWidth = 5f
                                                    }
                                            overlays.add(line)

                                            val boundingBox =
                                                    org.osmdroid.util.BoundingBox.fromGeoPoints(
                                                            listOf(originPoint, destPoint)
                                                    )
                                            zoomToBoundingBox(boundingBox, false, 100)
                                            isMapReady = true
                                        } catch (e: Exception) {
                                            // Silently handle map setup errors
                                        }
                                    }
                                } else {
                                    // If coordinates not found, set default view
                                    post {
                                        controller.setZoom(3.0)
                                        controller.setCenter(org.osmdroid.util.GeoPoint(36.8065, 10.1815))
                                        isMapReady = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            hasError = true
                            // Return empty MapView on error
                            MapView(ctx)
                        }
                    },
                    update = { mapView ->
                        // Optional: update map if needed
                    }
            )

            // Loading indicator
            if (!isMapReady && !hasError) {
                Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                    )
                }
            }
        }

        // Error fallback
        if (hasError) {
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .background(
                                            brush =
                                                    Brush.verticalGradient(
                                                            colors =
                                                                    listOf(
                                                                            Color(0xFFE3F2FD),
                                                                            Color(0xFFBBDEFB)
                                                                    )
                                                    )
                                    ),
                    contentAlignment = Alignment.Center
            ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                            Icons.Default.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                    )
                    Text(
                            text = "$originCity → $destinationCity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                            text = "Carte non disponible",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FlightInfoSection(flightOffer: FlightOffer) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
                text = "Informations du vol",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
        )

        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(
                                            brush =
                                                    Brush.verticalGradient(
                                                            colors =
                                                                    listOf(
                                                                            MaterialTheme.colorScheme.surface,
                                                                            MaterialTheme.colorScheme.primary.copy(
                                                                                    alpha = 0.05f
                                                                            )
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
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        InfoRow(icon = Icons.Default.Info, label = "Numéro de vol", value = it)
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    InfoRow(
                            icon = Icons.Default.Schedule,
                            label = "Type de vol",
                            value =
                                    when (flightOffer.getTypeValue()) {
                                        "aller-retour" -> "Aller-retour"
                                        "multi-destin" -> "Multi-destinations"
                                        else -> "Aller simple"
                                    }
                    )

                    if (flightOffer.availableSeats != null) {
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        InfoRow(
                                icon = Icons.Default.EventSeat,
                                label = "Places disponibles",
                                value = "${flightOffer.availableSeats} places"
                        )
                    }

                    flightOffer.direct?.let { isDirect ->
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        InfoRow(
                                icon = Icons.Default.Flight,
                                label = "Type de trajet",
                                value = if (isDirect) "Vol direct" else "Avec escale(s)"
                        )
                    }

                    flightOffer.stops?.let { stops ->
                        if (stops > 0) {
                            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
                text = "Détails des aéroports",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
        )

        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(
                                            brush =
                                                    Brush.verticalGradient(
                                                            colors =
                                                                    listOf(
                                                                            MaterialTheme.colorScheme.surface,
                                                                            ColorSecondary.copy(
                                                                                    alpha = 0.05f
                                                                            )
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                            )
                            Text(
                                    text = "Aéroport de départ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text =
                                        "${fromAirport.code.ifEmpty { "N/A" }} - ${fromAirport.name.ifEmpty { "Aéroport" }}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        fromAirport.city?.let {
                            Text(
                                    text = it,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        fromAirport.country?.let {
                            Text(
                                    text = it,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                    // Arrival Airport
                    Column {
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                    Icons.Default.FlightLand,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                            )
                            Text(
                                    text = "Aéroport d'arrivée",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text =
                                        "${toAirport.code.ifEmpty { "N/A" }} - ${toAirport.name.ifEmpty { "Aéroport" }}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        toAirport.city?.let {
                            Text(
                                    text = it,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        toAirport.country?.let {
                            Text(
                                    text = it,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PriceBreakdownCard(flightOffer: FlightOffer) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
                text = "Détail des prix",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
        )

        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(
                                            brush =
                                                    Brush.verticalGradient(
                                                            colors =
                                                                    listOf(
                                                                            MaterialTheme.colorScheme.surface,
                                                                            ColorAccent.copy(
                                                                                    alpha = 0.05f
                                                                            )
                                                                    )
                                                    )
                                    )
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PriceRow("Prix du vol", flightOffer.getFormattedPrice())
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    PriceRow("Total", flightOffer.getFormattedPrice(), isTotal = true)
                }
            }
        }
    }
}

@Composable
fun PriceRow(label: String, price: String, isTotal: Boolean = false) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = label,
                fontSize = if (isTotal) 16.sp else 14.sp,
                fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
                color = if (isTotal) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
                text = price,
                fontSize = if (isTotal) 20.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
