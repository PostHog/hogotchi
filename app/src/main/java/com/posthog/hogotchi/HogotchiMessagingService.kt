package com.posthog.hogotchi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.posthog.PostHog

class HogotchiMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        val title: String
        val body: String
        val action: String?

        if (remoteMessage.notification != null) {
            title = remoteMessage.notification?.title ?: "Hogotchi"
            body = remoteMessage.notification?.body ?: "Your hog needs attention!"
            action = remoteMessage.data["action"]
        } else if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            title = remoteMessage.data["title"] ?: "Hogotchi"
            body = remoteMessage.data["body"] ?: "Your hog needs attention!"
            action = remoteMessage.data["action"]
        } else {
            return
        }

        sendNotification(title, body, action)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        PostHog.setFcmToken(token)
        Log.d(TAG, "FCM token sent to PostHog")
    }

    private fun sendNotification(title: String, body: String, action: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            action?.let { putExtra("action", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(getColor(R.color.posthog_orange))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Hogotchi Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "HogotchiMsgService"
    }
}
