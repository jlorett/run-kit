package com.joshualorett.fusedapp.session

import com.joshualorett.fusedapp.time.TimeTracker
import com.joshualorett.fusedapp.toIsoString
import com.joshualorett.runkit.location.Location
import com.joshualorett.runkit.location.LocationTracker
import com.joshualorett.runkit.session.ActiveSessionDao
import com.joshualorett.runkit.session.Session
import com.joshualorett.runkit.session.SessionDao
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * The main point of access to the active running session.
 * Created by Joshua on 2/12/2021.
 */
class FusedActiveSessionRepository(private val sessionDao: SessionDao,
                                   private val activeSessionDao: ActiveSessionDao,
                                   private val timeTracker: TimeTracker,
                                   private val locationTracker: LocationTracker) {
    private var lastLocation: Location? = null
    val session: Flow<Session> = activeSessionDao.getActiveSessionFlow()

    fun start(scope: CoroutineScope) {
        scope.launch {
            var id = getCurrentSessionId()
            if (id == 0L) {
                id = sessionDao.createSession()
            }
            sessionDao.setSessionState(id, Session.State.STARTED)
            launch {
                timeTracker.start(session.first().elapsedTime)
                recordElapsedTime(coroutineContext)
            }
            launch {
                recordLocation(coroutineContext)
            }
        }
    }

    suspend fun pause() {
        val id = getCurrentSessionId()
        sessionDao.setSessionState(id, Session.State.PAUSED)
        timeTracker.stop()
        lastLocation = null
    }

    suspend fun stop()  {
        val endTime = Date().toIsoString()
        val id = getCurrentSessionId()
        sessionDao.setSessionState(id, Session.State.STOPPED)
        sessionDao.setEndTime(id, endTime)
        timeTracker.stop()
        lastLocation = null
    }

    suspend fun setElapsedTime(time: Long) {
        val id = getCurrentSessionId()
        sessionDao.setElapsedTime(id, time)
    }

    suspend fun setDistance(distance: Double) {
        val id = getCurrentSessionId()
        sessionDao.setDistance(id, distance)
    }

    suspend fun addLocation(location: Location) {
        val id = getCurrentSessionId()
        sessionDao.addLocation(id, location)
    }

    private suspend fun getCurrentSessionId(): Long {
        return activeSessionDao.getActiveSessionFlow().first().id
    }

    private suspend fun recordElapsedTime(coroutineContext: CoroutineContext, delayMs: Long = 1000) = withContext(coroutineContext) {
        while(true) {
            delay(delayMs)
            ensureActive()
            val inSession = session.first().state == Session.State.STARTED
            if(!inSession) {
                cancel()
            } else {
                withContext(Dispatchers.Default) {
                    setElapsedTime(timeTracker.getElapsedTime())
                }
            }
        }
    }

    private suspend fun recordLocation(coroutineContext: CoroutineContext) = withContext(coroutineContext) {
        locationTracker.track().collect { location ->
            ensureActive()
            val inSession = session.first().state == Session.State.STARTED
            if(!inSession) {
                cancel()
            }
            var totalDistance = withContext(Dispatchers.Default) {
                session.first().distance
            }
            lastLocation?.let {
                totalDistance += location.distanceTo(it)
            }
            lastLocation = location
            withContext(Dispatchers.Default) {
                setDistance(totalDistance)
                addLocation(location)
            }
        }
    }
}