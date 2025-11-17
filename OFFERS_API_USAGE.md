# Flight Offers API Integration - Usage Guide

## Overview
This document describes how to use the `/offers` API endpoint in the Android app. The integration includes data models, Retrofit interface, service layer, ViewModel, and UI components.

## API Endpoint
- **Base URL**: `http://10.0.2.2:3000` (from `SocketConfig.SERVER_URL`)
- **Endpoint**: `/offers`
- **Method**: `GET`

## Query Parameters
The API supports the following query parameters:

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `q` | String? | Search query (keyword search) | `"paris"` |
| `type` | String? | Flight type | `"aller-retour"`, `"aller-simple"`, `"multi-destin"` |
| `from` | String? | Departure airport code | `"TUN"` |
| `to` | String? | Arrival airport code | `"ORY"` |
| `direct` | Boolean? | Filter for direct flights only | `true` or `false` |
| `date_depart` | String? | Departure date | `"2024-11-18"` |
| `date_return` | String? | Return date (for round trips) | `"2024-11-25"` |
| `sort` | String? | Sort by | `"price"`, `"duration"`, `"departure_time"` |

## Data Models

### FlightOffer
```kotlin
data class FlightOffer(
    val id: String? = null,
    val airline: String,
    val flightNumber: String? = null,
    val type: String, // "aller-retour", "aller-simple", "multi-destin"
    val from: Airport,
    val to: Airport,
    val departure: FlightSegment,
    val returnSegment: FlightSegment? = null,
    val price: Double,
    val currency: String = "EUR",
    val duration: String? = null,
    val direct: Boolean = true,
    val stops: Int = 0,
    val availableSeats: Int? = null,
    val imageUrl: String? = null,
    val createdAt: String? = null
)
```

### Airport
```kotlin
data class Airport(
    val code: String, // e.g., "TUN", "ORY"
    val name: String, // e.g., "Tunis Carthage", "Paris Orly"
    val city: String? = null,
    val country: String? = null
)
```

### FlightSegment
```kotlin
data class FlightSegment(
    val flightNumber: String? = null,
    val airline: String? = null,
    val departure: SegmentDetails,
    val arrival: SegmentDetails,
    val duration: String, // Format: "2h 40min"
    val direct: Boolean = true,
    val stops: Int = 0
)
```

## Usage Examples

### 1. Get All Offers
```kotlin
// In ViewModel or Composable
val viewModel: OffersViewModel = hiltViewModel()

// Load all offers
viewModel.loadAllOffers()

// Observe offers
val offers by viewModel.offers.collectAsState()
```

### 2. Get Round-Trips from Tunis to Paris
```kotlin
// Using ViewModel
viewModel.getRoundTripsFromTunisToParis(
    dateDepart = "2024-11-18",
    dateReturn = "2024-11-25"
)

// Or directly using Service
val result = offersService.getRoundTripsFromTunisToParis(
    dateDepart = "2024-11-18",
    dateReturn = "2024-11-25"
)
```

### 3. Search with Keyword, Filter, and Sort
```kotlin
// Example: q=paris&type=aller-retour&direct=true&sort=price
viewModel.searchWithFilters(
    query = "paris",
    type = "aller-retour",
    directOnly = true,
    sortBy = "price"
)
```

### 4. Custom Search with All Parameters
```kotlin
// Set filters
viewModel.setSearchQuery("paris")
viewModel.setType("aller-retour")
viewModel.setFromAirport("TUN")
viewModel.setToAirport("ORY")
viewModel.setDirectOnly(true)
viewModel.setDateDepart("2024-11-18")
viewModel.setDateReturn("2024-11-25")
viewModel.setSortBy("price")

// Execute search
viewModel.searchOffers()
```

### 5. Using OffersService Directly
```kotlin
@Inject
lateinit var offersService: OffersService

// Get all offers
val result = offersService.getAllOffers()
result.onSuccess { offers ->
    // Handle offers
    println("Received ${offers.size} offers")
}.onFailure { error ->
    // Handle error
    println("Error: ${error.message}")
}

// Get offers with filters
val result = offersService.getOffers(
    q = "paris",
    type = "aller-retour",
    from = "TUN",
    to = "ORY",
    direct = true,
    date_depart = "2024-11-18",
    date_return = "2024-11-25",
    sort = "price"
)
```

## UI Components

### OffresScreen
The main screen that displays flight offers with:
- **Search Bar**: Search by keyword
- **Flight Type Selector**: Filter by type (Aller-retour, Aller simple, Multi-destin)
- **Filter Button**: Open filters panel (from, to, dates)
- **Sort Button**: Sort by price, duration, departure time
- **Direct Flights Toggle**: Filter for direct flights only
- **Flight Offer Cards**: Display each offer with airline, times, airports, duration, price

### Example API Calls from UI

1. **Search for "paris"**:
   - User types "paris" in search bar
   - Calls `viewModel.setSearchQuery("paris")` and `viewModel.searchOffers()`

2. **Filter round trips from TUN to ORY**:
   - User selects "Aller-retour" type
   - Sets from="TUN", to="ORY"
   - Calls `viewModel.searchOffers()`

3. **Sort by price**:
   - User clicks "Trier" → selects "Les moins chers"
   - Calls `viewModel.setSortBy("price")` and `viewModel.searchOffers()`

## Error Handling

The service returns `Result<List<FlightOffer>>` which can be handled with:
```kotlin
val result = offersService.getOffers(...)
result.onSuccess { offers ->
    // Success: use offers
}.onFailure { error ->
    // Error: handle error
    Log.e("Offers", "Error: ${error.message}")
}
```

In the ViewModel, errors are exposed via `error: StateFlow<String?>`:
```kotlin
val error by viewModel.error.collectAsState()
error?.let { errorMessage ->
    // Display error to user
}
```

## State Management

The `OffersViewModel` manages:
- `offers: StateFlow<List<FlightOffer>>` - Current list of offers
- `isLoading: StateFlow<Boolean>` - Loading state
- `error: StateFlow<String?>` - Error message (if any)
- Filter states (searchQuery, selectedType, fromAirport, etc.)

## Files Created

1. **Data Models**:
   - `app/src/main/java/com/travelmate/data/models/FlightOffer.kt`
   - `app/src/main/java/com/travelmate/data/models/Airport.kt` (included in FlightOffer.kt)
   - `app/src/main/java/com/travelmate/data/models/FlightSegment.kt` (included in FlightOffer.kt)

2. **API Interface**:
   - `app/src/main/java/com/travelmate/data/api/OffersApi.kt`

3. **Service Layer**:
   - `app/src/main/java/com/travelmate/data/service/OffersService.kt`

4. **ViewModel**:
   - `app/src/main/java/com/travelmate/viewmodel/OffersViewModel.kt`

5. **UI**:
   - `app/src/main/java/com/travelmate/ui/screens/user/OffresScreen.kt`

6. **Dependency Injection**:
   - Updated `app/src/main/java/com/travelmate/di/NetworkModule.kt` to provide `OffersApi`

## Notes

- All API calls are asynchronous and use Kotlin Coroutines
- The service automatically handles loading states and errors
- The ViewModel exposes reactive StateFlows for UI observation
- The UI uses Jetpack Compose with Material 3 design
- Images are loaded using URLs (no Cloudinary integration)

