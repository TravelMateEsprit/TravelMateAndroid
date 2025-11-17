package com.travelmate.data.models

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

@Serializable
data class MessageGroupe(
    @SerialName("_id")
    val id: String = "",

    @SerialName("groupId")
    val groupId: String = "",

    @SerialName("authorId")
    @Serializable(with = AuthorIdSerializer::class)  // ✅ Custom serializer
    val authorId: AuthorInfo? = null,

    @SerialName("content")
    val content: String = "",

    @SerialName("images")
    val images: List<String> = emptyList(),

    @SerialName("status")
    val status: String = "publie",

    @SerialName("createdAt")
    val createdAt: String = "",

    @SerialName("updatedAt")
    val updatedAt: String = ""
)

@Serializable
data class AuthorInfo(
    @SerialName("_id")
    val id: String = "",

    @SerialName("nom")
    val nom: String? = null,

    @SerialName("prenom")
    val prenom: String? = null,

    @SerialName("email")
    val email: String? = null
)

// ✅ NOUVEAU : Serializer personnalisé pour gérer string OU object
object AuthorIdSerializer : KSerializer<AuthorInfo?> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("AuthorInfo") {
            element<String>("_id")
            element<String?>("nom")
            element<String?>("prenom")
            element<String?>("email")
        }

    override fun serialize(encoder: Encoder, value: AuthorInfo?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeSerializableValue(AuthorInfo.serializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): AuthorInfo? {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonInput")
        val element = input.decodeJsonElement()

        return when {
            element is JsonNull -> null

            // ✅ Cas 1 : authorId est une string (update/create)
            element is JsonPrimitive && element.isString -> {
                AuthorInfo(id = element.content)
            }

            // ✅ Cas 2 : authorId est un objet (getMessages avec populate)
            element is JsonObject -> {
                input.json.decodeFromJsonElement(AuthorInfo.serializer(), element)
            }

            else -> null
        }
    }
}