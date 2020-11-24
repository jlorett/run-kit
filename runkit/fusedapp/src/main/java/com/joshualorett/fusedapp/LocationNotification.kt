package com.joshualorett.fusedapp

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Create a notification for the current location.
 * Created by Joshua on 10/17/2020.
 */
fun createLocationNotification(context: Context, title: String, content: String, channelId: String,
                               contentIntent: Intent, stopActionIntent: Intent): Notification {
    val contentPendingIntent = PendingIntent.getActivity(context, 0,
        contentIntent, 0)
    val stopActionPendingIntent = PendingIntent.getService(context, 0,
        stopActionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    val stopActionText = context.getString(R.string.stop)
    return NotificationCompat.Builder(context, channelId)
        .setContentIntent(contentPendingIntent)
        .addAction(R.drawable.ic_baseline_close_24, stopActionText, stopActionPendingIntent)
        .setContentTitle(title)
        .setContentText(content)
        .setOngoing(true)
        .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
        .setSmallIcon(R.drawable.ic_run_24)
        .setTicker(content)
        .setWhen(System.currentTimeMillis())
        .setChannelId(channelId)
        .build()
}