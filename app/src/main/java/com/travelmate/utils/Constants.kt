package com.travelmate.utils

object Constants {
    
    // Navigation Routes
    object Routes {
        const val WELCOME = "welcome"
        const val USER_REGISTRATION = "user_registration"
        const val AGENCY_REGISTRATION = "agency_registration"
        const val LOGIN = "login"
        const val HOME = "home"
        const val USER_HOME = "user_home"
        const val AGENCY_DASHBOARD = "agency_dashboard"
        const val RESERVATION = "reservation/{voyageId}"
        const val MY_RESERVATIONS = "my_reservations"
        const val VOYAGE_FORM = "voyage_form"
        const val VOYAGE_EDIT = "voyage_edit/{voyageId}"
        const val VOYAGE_DETAIL = "voyage_detail/{voyageId}"
    }
    
    // SharedPreferences Keys
    object PrefsKeys {
        const val AUTH_TOKEN = "auth_token"
        const val USER_ID = "user_id"
        const val USER_TYPE = "user_type"
        const val USER_EMAIL = "user_email"
    }
    
    // User Types
    object UserTypes {
        const val USER = "USER"
        const val AGENCY = "AGENCY"
        const val ADMIN = "ADMIN"
    }
    
    // Date Formats
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm" // Format for API: YYYY-MM-DD HH:mm
    const val DISPLAY_DATE_FORMAT = "dd/MM/yyyy"
    const val DISPLAY_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm"
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 8
    const val SIRET_LENGTH = 14
    const val POSTAL_CODE_LENGTH = 5
}
