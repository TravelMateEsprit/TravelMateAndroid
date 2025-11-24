package com.travelmate.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.travelmate.MainActivity
import com.travelmate.R
import com.travelmate.data.model.NotificationModel
import com.travelmate.data.model.NotificationType
import com.travelmate.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "travelmate_notifications"
        const val CHANNEL_NAME = "TravelMate Notifications"
        
        // Intent extras keys
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_REQUEST_ID = "request_id"
        const val EXTRA_PAYMENT_ID = "payment_id"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Appelé quand Firebase génère un nouveau token FCM
     * Il faut l'envoyer au backend
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // Enregistrer le token dans le backend
        serviceScope.launch {
            try {
                val result = notificationRepository.registerFcmToken(token)
                if (result.isSuccess) {
                    Log.d(TAG, "Token successfully registered with backend")
                } else {
                    Log.e(TAG, "Failed to register token: ${result.exceptionOrNull()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering token", e)
            }
        }
    }

    /**
     * Appelé quand une notification est reçue
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Vérifier si le message contient des données
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Vérifier si le message contient une notification
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(
                title = it.title ?: "TravelMate",
                body = it.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    /**
     * Traite les messages de type "data"
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return
        val title = data["title"] ?: "TravelMate"
        val body = data["body"] ?: ""
        
        // Créer une notification locale dans le repository
        try {
            val notificationType = when (type) {
                "NEW_INSURANCE_REQUEST" -> NotificationType.NEW_INSURANCE_REQUEST
                "REQUEST_STATUS_CHANGED" -> NotificationType.REQUEST_STATUS_CHANGED
                "PAYMENT_CONFIRMED" -> NotificationType.PAYMENT_CONFIRMED
                "PAYMENT_FAILED" -> NotificationType.PAYMENT_FAILED
                "NEW_INSURANCE_PRODUCT" -> NotificationType.NEW_INSURANCE_PRODUCT
                "SUBSCRIPTION_CONFIRMED" -> NotificationType.SUBSCRIPTION_CONFIRMED
                else -> NotificationType.NEW_INSURANCE_REQUEST
            }
            
            val notification = NotificationModel(
                _id = System.currentTimeMillis().toString(),
                userId = data["userId"] ?: "",
                type = notificationType,
                title = title,
                body = body,
                data = data.filterKeys { it != "title" && it != "body" && it != "type" },
                isRead = false,
                isSent = true,
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            )
            
            notificationRepository.addNotificationLocally(notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating local notification", e)
        }
        
        // Afficher la notification système
        sendNotification(title, body, data)
    }

    /**
     * Crée et affiche une notification Android
     */
    private fun sendNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            
            // Ajouter les données pour la navigation
            data["type"]?.let { putExtra(EXTRA_NOTIFICATION_TYPE, it) }
            data["requestId"]?.let { putExtra(EXTRA_REQUEST_ID, it) }
            data["paymentId"]?.let { putExtra(EXTRA_PAYMENT_ID, it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Icône selon le type de notification
        val icon = when (data["type"]) {
            "PAYMENT_CONFIRMED" -> R.drawable.ic_check_circle
            "PAYMENT_FAILED" -> R.drawable.ic_error
            "REQUEST_STATUS_CHANGED" -> R.drawable.ic_info
            else -> R.drawable.ic_notifications
        }
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * Crée le canal de notification (requis pour Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les demandes d'assurance et paiements"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            
            Log.d(TAG, "Notification channel created")
        }
    }
}
