package com.travelmate.data.models

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

@Serializable
data class UserReactionInfo(
    @SerialName("_id")
    val id: String,

    @SerialName("nom")
    val nom: String? = null,

    @SerialName("prenom")
    val prenom: String? = null,

    @SerialName("email")
    val email: String? = null
)

// ✅ NOUVEAU: Deserializer flexible pour userId
object UserReactionInfoSerializer : KSerializer<UserReactionInfo> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("UserReactionInfo")

    override fun serialize(encoder: Encoder, value: UserReactionInfo) {
        encoder.encodeSerializableValue(UserReactionInfo.serializer(), value)
    }

    override fun deserialize(decoder: Decoder): UserReactionInfo {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonInput")
        val element = input.decodeJsonElement()

        return when {
            // ✅ Si c'est une String (userId simple)
            element is JsonPrimitive && element.isString -> {
                UserReactionInfo(
                    id = element.content,
                    nom = null,
                    prenom = null,
                    email = null
                )
            }
            // ✅ Si c'est un objet complet
            element is JsonObject -> {
                input.json.decodeFromJsonElement(UserReactionInfo.serializer(), element)
            }
            else -> throw SerializationException("Invalid userId format: $element")
        }
    }
}

@Serializable
data class MessageReaction(
    @SerialName("userId")
    @Serializable(with = UserReactionInfoSerializer::class) // ✅ Utiliser le deserializer
    val userId: UserReactionInfo,

    @SerialName("emoji")
    val emoji: String,

    @SerialName("reactedAt")
    val reactedAt: String = ""
)

@Serializable
data class MessageGroupe(
    @SerialName("_id")
    val id: String = "",

    @SerialName("groupId")
    val groupId: String = "",

    @SerialName("authorId")
    @Serializable(with = AuthorIdSerializer::class)
    val authorId: AuthorInfo? = null,

    @SerialName("content")
    val content: String = "",

    @SerialName("images")
    val images: List<String> = emptyList(),

    @SerialName("reactions")
    val reactions: List<MessageReaction> = emptyList(),

    @SerialName("status")
    val status: String = "publie",

    @SerialName("createdAt")
    val createdAt: String = "",

    @SerialName("updatedAt")
    val updatedAt: String = "",

    @SerialName("tempId")
    val tempId: String? = null
) {
    val isTemporary: Boolean get() = id.startsWith("temp_") || tempId != null
    val uniqueId: String get() = if (isTemporary) "temp_${tempId ?: id}" else id
}

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
            element is JsonPrimitive && element.isString -> {
                AuthorInfo(id = element.content)
            }
            element is JsonObject -> {
                input.json.decodeFromJsonElement(AuthorInfo.serializer(), element)
            }
            else -> null
        }
    }
}