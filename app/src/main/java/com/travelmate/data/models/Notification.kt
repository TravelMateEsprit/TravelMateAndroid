package com.travelmate.data.models

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

@Serializable
data class Notification(
    @SerialName("_id")
    val id: String = "",
    
    @SerialName("userId")
    val userId: String = "",
    
    @SerialName("type")
    val type: NotificationType = NotificationType.GROUP_REQUEST,
    
    @SerialName("title")
    val title: String = "",
    
    @SerialName("message")
    val message: String = "",
    
    @SerialName("read")
    val read: Boolean = false,
    
    @SerialName("groupId")
    @Serializable(with = GroupIdSerializer::class)
    val groupId: String? = null,
    
    @SerialName("relatedUserId")
    @Serializable(with = RelatedUserIdSerializer::class)
    val relatedUserId: String? = null,
    
    @SerialName("createdAt")
    val createdAt: String = "",
    
    @SerialName("updatedAt")
    val updatedAt: String = ""
)

// Custom serializer pour gérer groupId qui peut être String ou Object
object GroupIdSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GroupId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "")
    }

    override fun deserialize(decoder: Decoder): String? {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonDecoder")
        val element = input.decodeJsonElement()
        
        return when {
            element is JsonNull -> null
            element is JsonPrimitive && element.isString -> {
                val str = element.content
                if (str.isBlank()) null else str
            }
            element is JsonObject -> {
                // Si c'est un objet, extraire l'ID
                element["_id"]?.let { 
                    if (it is JsonPrimitive) it.content else null
                } ?: element["id"]?.let {
                    if (it is JsonPrimitive) it.content else null
                }
            }
            else -> null
        }
    }
}

// Custom serializer pour gérer relatedUserId qui peut être String ou Object
object RelatedUserIdSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RelatedUserId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "")
    }

    override fun deserialize(decoder: Decoder): String? {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonDecoder")
        val element = input.decodeJsonElement()
        
        return when {
            element is JsonNull -> null
            element is JsonPrimitive && element.isString -> {
                val str = element.content
                if (str.isBlank()) null else str
            }
            element is JsonObject -> {
                // Si c'est un objet, extraire l'ID
                element["_id"]?.let { 
                    if (it is JsonPrimitive) it.content else null
                } ?: element["id"]?.let {
                    if (it is JsonPrimitive) it.content else null
                }
            }
            else -> null
        }
    }
}

@Serializable
enum class NotificationType {
    @SerialName("group_request")
    GROUP_REQUEST,
    
    @SerialName("group_approved")
    GROUP_APPROVED,
    
    @SerialName("group_rejected")
    GROUP_REJECTED,
    
    @SerialName("member_removed")
    MEMBER_REMOVED,
    
    @SerialName("member_banned")
    MEMBER_BANNED
}

@Serializable
data class UnreadCountResponse(
    @SerialName("count")
    val count: Int = 0
)

