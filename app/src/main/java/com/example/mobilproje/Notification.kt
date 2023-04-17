package com.example.mobilproje

import Data
import NotificationSender
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.mobilproje.databinding.NotificationBinding
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import javax.security.auth.callback.Callback

val channelId = "notification_channel"
val channelName = "com.example.mobilproje"

public class Notification : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        generateNotification("BİLDİRİM","GELDİ")
        /*if(message.getNotification() != null){
            message.notification!!.body?.let {
                message.notification!!.title?.let { it1 ->
                    generateNotification(
                        it1,
                        it
                    )
                }
            }
        }*/
    }
    override fun onNewToken(token: String) {

    }
    fun sendNotification(token: String, title: String, messageBody: String, activity: MainActivity) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(activity,
            0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(activity, channelId)
            .setSmallIcon(R.drawable.baseline_account_box_24_black)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = activity.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        FirebaseMessaging.getInstance().send(RemoteMessage.Builder(token)
            .setMessageId("Notification")
            .setData(mapOf("title" to title, "body" to messageBody))
            .build())

        notificationManager.notify(0, notificationBuilder.build())
    }



    fun getRemoteView(title: String, message: String) : RemoteViews{
        val remoteView = RemoteViews("com.example.mobilproje",R.layout.notification)
        remoteView.setTextViewText(R.id.title_textview,title)
        remoteView.setTextViewText(R.id.content_textview,message)
        return remoteView

    }
    fun generateNotification(title: String, message: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext,
        channelId).setSmallIcon(R.drawable.baseline_account_box_24_black).setAutoCancel(true)
            .setVibrate(longArrayOf(1000,1000,1000,1000)).setOnlyAlertOnce(true).setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(title,message))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0, builder.build())
    }
}