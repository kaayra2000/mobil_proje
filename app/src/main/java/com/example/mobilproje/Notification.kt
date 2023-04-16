package com.example.mobilproje

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.mobilproje.databinding.NotificationBinding
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage

val channelId = "notification_channel"
val channelName = "com.example.mobilproje"

class Notification : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if(message.getNotification() != null){
            message.notification!!.body?.let {
                message.notification!!.title?.let { it1 ->
                    generateNotification(
                        it1,
                        it
                    )
                }
            }
        }
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