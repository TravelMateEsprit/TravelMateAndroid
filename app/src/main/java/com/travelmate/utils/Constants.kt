package com.travelmate.utils

object Constants {
    
    // Navigation Routes
    object Routes {
        const val WELCOME = "welcome"
        const val USER_REGISTRATION = "user_registration"
        const val AGENCY_REGISTRATION = "agency_registration"
        const val LOGIN = "login"
        const val FORGOT_PASSWORD = "forgot_password"
        const val RESET_PASSWORD = "reset_password/{token}"
        const val HOME = "home"
        const val USER_HOME = "user_home"
        const val AGENCY_DASHBOARD = "agency_dashboard"
        const val FLIGHT_DETAILS = "flight_details"
        const val USER_PROFILE = "user_profile"
        const val AGENCY_PROFILE = "agency_profile"
        
        // Insurance Requests Routes
        const val CREATE_INSURANCE_REQUEST = "create_insurance_request"
        const val MY_INSURANCE_REQUESTS = "my_insurance_requests"
        const val REQUEST_DETAILS = "request_details"
        const val AGENCY_INSURANCE_REQUESTS = "agency_insurance_requests"
        const val REVIEW_REQUEST = "review_request"
        const val PAYMENT = "payment"
        
        // Claims Routes - User
        const val MY_CLAIMS = "my_claims"
        const val CREATE_CLAIM = "create_claim"
        const val CLAIM_DETAIL = "claim_detail"
        
        // Claims Routes - Agency
        const val AGENCY_CLAIMS = "agency_claims"
        const val AGENCY_CLAIM_DETAIL = "agency_claim_detail"
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
    const val DISPLAY_DATE_FORMAT = "dd/MM/yyyy"
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 8
    const val SIRET_LENGTH = 14
    const val POSTAL_CODE_LENGTH = 5
}
