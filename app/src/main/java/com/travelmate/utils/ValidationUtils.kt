package com.travelmate.utils

import android.util.Patterns

object ValidationUtils {
    
    /**
     * Valide le format d'une adresse email
     */
    fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Valide un mot de passe
     * Règles: min 8 caractères, 1 majuscule, 1 chiffre
     */
    fun validatePassword(password: String): Boolean {
        if (password.length < 8) return false
        
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return hasUpperCase && hasDigit
    }
    
    /**
     * Valide un numéro SIRET (14 chiffres)
     */
    fun validateSiret(siret: String): Boolean {
        val cleanedSiret = siret.replace("\\s".toRegex(), "")
        return cleanedSiret.length == 14 && cleanedSiret.all { it.isDigit() }
    }
    
    /**
     * Valide un numéro de téléphone (format international ou français)
     */
    fun validatePhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        val phoneRegex = "^[+]?[0-9\\s()-]{8,20}$".toRegex()
        return phoneRegex.matches(phone)
    }
    
    /**
     * Valide un code postal français
     */
    fun validatePostalCode(postalCode: String): Boolean {
        val postalCodeRegex = "^[0-9]{5}$".toRegex()
        return postalCodeRegex.matches(postalCode)
    }
    
    /**
     * Valide une URL
     */
    fun validateUrl(url: String): Boolean {
        if (url.isBlank()) return true // URL optionnelle
        return Patterns.WEB_URL.matcher(url).matches()
    }
    
    /**
     * Retourne un message d'erreur pour l'email
     */
    fun getEmailError(email: String): String? {
        return when {
            email.isBlank() -> "L'email est obligatoire"
            !validateEmail(email) -> "Format email invalide"
            else -> null
        }
    }
    
    /**
     * Retourne un message d'erreur pour le mot de passe
     */
    fun getPasswordError(password: String): String? {
        return when {
            password.isBlank() -> "Le mot de passe est obligatoire"
            password.length < 8 -> "Minimum 8 caractères"
            !password.any { it.isUpperCase() } -> "Au moins 1 majuscule requise"
            !password.any { it.isDigit() } -> "Au moins 1 chiffre requis"
            else -> null
        }
    }
}
