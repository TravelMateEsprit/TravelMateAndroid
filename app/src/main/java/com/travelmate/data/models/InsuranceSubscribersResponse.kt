package com.travelmate.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject

@Serializable
data class InsuranceSubscribersResponse(
    val insuranceName: String,
    val subscribersCount: Int,
    val subscribers: JsonElement // Peut être un tableau de strings (IDs) ou d'objets User
) {
    // Fonction helper pour extraire les User objects si disponibles
    fun getSubscriberUsers(): List<User> {
        return try {
            android.util.Log.d("InsuranceSubscribersResponse", "=== PARSING SUBSCRIBERS ===")
            android.util.Log.d("InsuranceSubscribersResponse", "Subscribers type: ${subscribers::class.simpleName}")
            android.util.Log.d("InsuranceSubscribersResponse", "Subscribers raw: $subscribers")
            
            if (subscribers is JsonArray) {
                val array = subscribers as JsonArray
                android.util.Log.d("InsuranceSubscribersResponse", "Array size: ${array.size}")
                
                if (array.isEmpty()) {
                    android.util.Log.d("InsuranceSubscribersResponse", "Array is empty")
                    emptyList()
                } else {
                    // Vérifier si le premier élément est un objet ou une string
                    val firstElement = array.first()
                    android.util.Log.d("InsuranceSubscribersResponse", "First element: $firstElement")
                    
                    try {
                        // Essayer d'accéder à jsonPrimitive - si ça marche, c'est une string
                        val primitiveValue = firstElement.jsonPrimitive.content
                        android.util.Log.d("InsuranceSubscribersResponse", "Subscribers are IDs only: $primitiveValue")
                        // C'est un tableau d'IDs, on ne peut pas créer des objets User
                        emptyList()
                    } catch (e: Exception) {
                        android.util.Log.d("InsuranceSubscribersResponse", "Subscribers are objects, parsing...")
                        // C'est un tableau d'objets User
                        val users = array.map { element ->
                            val obj = element.jsonObject
                            val name = obj["name"]?.jsonPrimitive?.content ?: ""
                            val email = obj["email"]?.jsonPrimitive?.content ?: ""
                            val phone = obj["phone"]?.jsonPrimitive?.content
                            
                            android.util.Log.d("InsuranceSubscribersResponse", "Parsing user: name=$name, email=$email, phone=$phone")
                            
                            // Diviser le name en firstName et lastName
                            val nameParts = name.split(" ", limit = 2)
                            val firstName = nameParts.firstOrNull() ?: ""
                            val lastName = if (nameParts.size > 1) nameParts[1] else ""
                            
                            User(
                                _id = obj["_id"]?.jsonPrimitive?.content ?: "",
                                email = email,
                                name = name,
                                userType = obj["userType"]?.jsonPrimitive?.content ?: "user",
                                status = obj["status"]?.jsonPrimitive?.content ?: "active",
                                phone = phone,
                                address = obj["address"]?.jsonPrimitive?.content
                            )
                        }
                        android.util.Log.d("InsuranceSubscribersResponse", "Successfully parsed ${users.size} users")
                        users
                    }
                }
            } else {
                android.util.Log.e("InsuranceSubscribersResponse", "Subscribers is not a JsonArray")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("InsuranceSubscribersResponse", "Error parsing subscribers: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }
}
