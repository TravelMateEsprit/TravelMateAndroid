# TravelMate Android

A modern Android application for managing travel insurance, built with Jetpack Compose and Clean Architecture principles.

## 📱 Overview

TravelMate is a comprehensive travel insurance management platform that connects users with insurance agencies. The application provides a seamless experience for browsing, subscribing to, and managing travel insurance policies.

## ✨ Features

### For Users
- **Browse Insurance Plans** - Explore various travel insurance options from different agencies
- **Subscribe to Insurance** - Easy subscription process with real-time updates
- **Profile Management** - Manage personal information and view active subscriptions
- **Real-time Notifications** - Receive instant updates via Socket.IO integration

### For Agencies
- **Insurance Management** - Create, update, and manage insurance offerings
- **Dashboard Analytics** - View subscribers and manage insurance portfolios
- **Agency Registration** - Dedicated registration process with SIRET validation
- **Subscriber Tracking** - Monitor and manage insurance subscribers

### General Features
- **Secure Authentication** - JWT-based authentication with encrypted local storage
- **Modern UI/UX** - Beautiful Material Design 3 interface with Jetpack Compose
- **Real-time Communication** - Socket.IO integration for live updates
- **Offline Support** - Encrypted SharedPreferences for secure data persistence

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin** - Modern programming language for Android
- **Jetpack Compose** - Declarative UI framework
- **Material Design 3** - Latest Material Design components

### Architecture & Design Patterns
- **MVVM Architecture** - Model-View-ViewModel pattern
- **Clean Architecture** - Separation of concerns with layered structure
- **Repository Pattern** - Data layer abstraction
- **Dependency Injection** - Hilt for dependency management

### Libraries & Frameworks
- **Hilt** - Dependency injection framework
- **Retrofit** - Type-safe REST API client
- **OkHttp** - HTTP client with logging interceptor
- **Socket.IO** - Real-time bidirectional communication
- **Kotlinx Serialization** - JSON serialization/deserialization
- **Coil** - Image loading library for Compose
- **Navigation Compose** - Type-safe navigation
- **Encrypted SharedPreferences** - Secure local data storage
- **Coroutines** - Asynchronous programming

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK (API level 24 or higher)
- Kotlin 1.9.x or later

## 🚀 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/TravelMateEsprit/TravelMateAndroid.git
   cd TravelMateAndroid
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned repository

3. **Sync Gradle**
   - Wait for Android Studio to sync Gradle dependencies
   - Ensure all dependencies are downloaded successfully

4. **Configure Backend URL**
   - Update the backend server URL in `app/src/main/java/com/travelmate/data/socket/SocketConfig.kt`
   - Set your backend API endpoint

5. **Build the project**
   ```bash
   ./gradlew build
   ```

## 📂 Project Structure

```
app/src/main/java/com/travelmate/
├── data/
│   ├── api/              # REST API interfaces
│   ├── models/           # Data models
│   ├── repository/       # Repository implementations
│   ├── service/          # Service layer
│   └── socket/           # Socket.IO configuration
├── di/                   # Dependency injection modules
│   ├── NetworkModule.kt  # Network dependencies
│   └── SocketModule.kt   # Socket.IO dependencies
├── ui/
│   ├── components/       # Reusable UI components
│   ├── navigation/       # Navigation graph
│   ├── screens/          # Application screens
│   │   ├── agency/       # Agency-specific screens
│   │   ├── login/        # Authentication screens
│   │   ├── registration/ # Registration flows
│   │   ├── user/         # User-specific screens
│   │   └── welcome/      # Welcome/onboarding
│   ├── theme/            # App theme and styling
│   └── viewmodels/       # ViewModels
├── utils/                # Utility classes
│   ├── Constants.kt      # App constants
│   ├── UserPreferences.kt# Secure preferences
│   └── ValidationUtils.kt# Input validation
├── MainActivity.kt       # Main activity
└── TravelMateApp.kt      # Application class
```

## ⚙️ Configuration

### Minimum SDK Configuration
- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 34 (Android 14)
- **compileSdk**: 34

### Build Variants
- **Debug** - Development build with logging enabled
- **Release** - Production build with ProGuard optimization

### Required Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

## 🏃 Running the Application

### Using Android Studio
1. Select a device or emulator
2. Click the "Run" button (Shift + F10)
3. Wait for the build to complete

### Using Command Line
```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

## 🧪 Testing

The project includes both unit tests and instrumentation tests:

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

## 🔐 Security Features

- **Encrypted Storage** - Sensitive data encrypted using AndroidX Security
- **JWT Authentication** - Secure token-based authentication
- **Network Security** - Custom network security configuration
- **HTTPS Enforcement** - Secure communication with backend
- **Input Validation** - Comprehensive validation for user inputs

## 🎨 Design System

The app follows Material Design 3 guidelines with:
- Dynamic color theming
- Responsive layouts for different screen sizes
- Consistent typography and spacing
- Accessible UI components
- Dark mode support

## 🔄 State Management

- **ViewModel** - UI state management
- **StateFlow** - Reactive state updates
- **Coroutines** - Asynchronous operations
- **Repository Pattern** - Single source of truth

## 📱 Supported Features by User Type

| Feature | User | Agency |
|---------|------|--------|
| Browse Insurance | ✅ | ✅ |
| Subscribe to Insurance | ✅ | ❌ |
| Create Insurance | ❌ | ✅ |
| Manage Insurance | ❌ | ✅ |
| View Subscribers | ❌ | ✅ |
| Profile Management | ✅ | ✅ |

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is developed as part of the TravelMate project at ESPRIT.

## 👥 Team

TravelMate Team - ESPRIT

## 📞 Support

For support and questions, please open an issue in the repository.

---

**Built with ❤️ using Jetpack Compose**
