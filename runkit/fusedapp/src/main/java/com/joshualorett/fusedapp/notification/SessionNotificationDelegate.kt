package com.joshualorett.fusedapp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.joshualorett.fusedapp.MainActivity
import com.joshualorett.fusedapp.R
import com.joshualorett.fusedapp.formatDistance
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.time.formatHourMinuteSeconds

/**
 * Delegate for session notification.
 * Created by Joshua on 12/27/2020.
 */
class SessionNotificationDelegate(private val context: Context,
                                  private val notificationManager: NotificationManager,
                                  private val notificationId: Int, private val channelId: String,
                                  private val extraToggleSessionAction: String, private val serviceClass: Class<*>) {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notify(session: Session) {
        notificationManager.notify(notificationId, getNotification(session))
    }

    fun getNotification(session: Session): Notification {
        val state = session.state
        val title = formatHourMinuteSeconds(session.elapsedTime)
        val formattedDistance = formatDistance(session.distance)
        val text = if (state == Session.State.PAUSED) "Paused - $formattedDistance" else formattedDistance
        val contentIntent = Intent(context, MainActivity::class.java)
        val toggleAction = getToggleAction(state)
        return SessionNotificationBuilder
            .toggleAction(toggleAction)
            .build(context, title, text, channelId, contentIntent)
    }

    private fun getToggleAction(state: Session.State): NotificationCompat.Action {
        val toggleActionIntent = Intent(context, serviceClass).also {
            it.putExtra(extraToggleSessionAction, true)
        }
        val toggleActionPendingIntent = PendingIntent.getService(context, 0,
            toggleActionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return when(state) {
            Session.State.STARTED ->
                NotificationCompat.Action(
                    R.drawable.ic_pause_24, context.getString(R.string.pause),
                    toggleActionPendingIntent)
            Session.State.PAUSED, Session.State.STOPPED ->
                NotificationCompat.Action(
                    R.drawable.ic_play_arrow_24, context.getString(R.string.resume),
                    toggleActionPendingIntent)
        }
    }
}