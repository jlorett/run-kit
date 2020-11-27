package com.joshualorett.fusedapp.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.joshualorett.fusedapp.R

/**
 * Builds the session notification.
 * Created by Joshua on 11/23/2020.
 */
object SessionNotificationBuilder {
    private var toggleAction: NotificationCompat.Action? = null

    fun toggleAction(action: NotificationCompat.Action): SessionNotificationBuilder {
        toggleAction = action
        return this
    }

    fun build(context: Context, title: String, content: String, channelId: String, contentIntent: Intent): Notification {
        val contentPendingIntent = PendingIntent.getActivity(context, 0,
            contentIntent, 0)
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentIntent(contentPendingIntent)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
            .setSmallIcon(R.drawable.ic_run_24)
            .setTicker(content)
            .setWhen(System.currentTimeMillis())
            .setChannelId(channelId)
        toggleAction?.let {
            builder.addAction(it)
        }
        return builder.build()
    }
}