package com.travelmate.ui.theme

import androidx.compose.ui.graphics.Color

// TravelMate Color Palette - Palette originale
val ColorPrimary = Color(0xFF2F80ED)
val ColorSecondary = Color(0xFF56CCF2)
val ColorAccent = Color(0xFFF2C94C)
val ColorBackground = Color(0xFFF9FAFB)

// Light theme text colors
val ColorTextPrimaryLight = Color(0xFF1F2937)
val ColorTextSecondaryLight = Color(0xFF6B7280)

// Dark theme text colors
val ColorTextPrimaryDark = Color(0xFFE5E7EB)
val ColorTextSecondaryDark = Color(0xFF9CA3AF)

// DEPRECATED: Use MaterialTheme.colorScheme.onSurface instead
@Deprecated("Use MaterialTheme.colorScheme.onSurface instead")
val ColorTextPrimary = Color(0xFF1F2937)

// DEPRECATED: Use MaterialTheme.colorScheme.onSurfaceVariant instead
@Deprecated("Use MaterialTheme.colorScheme.onSurfaceVariant instead")
val ColorTextSecondary = Color(0xFF6B7280)

// Additional colors
val ColorError = Color(0xFFDC2626)
val ColorSuccess = Color(0xFF10B981)
val ColorWarning = Color(0xFFF59E0B)
val ColorInfo = Color(0xFF3B82F6)

// Dark theme colors
val ColorPrimaryDark = Color(0xFF60A5FA)
val ColorSecondaryDark = Color(0xFF38BDF8)
val ColorBackgroundDark = Color(0xFF111827)
val ColorSurfaceDark = Color(0xFF1F2937)
val ColorDivider = Color(0xFFE5E7EB)
val ColorDividerDark = Color(0xFF374151)

// Legacy colors for backward compatibility - Use MaterialTheme.colorScheme instead
val PrimaryBlue = ColorPrimary
val AccentOrange = Color(0xFFFF9800)
val LightBlueBackground = Color(0xFFE3F2FD)
val TextSecondary = ColorTextSecondaryLight
val SuccessGreen = ColorSuccess
val ErrorRed = ColorError
val WarningYellow = ColorWarning
