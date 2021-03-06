package com.joshualorett.runkit.session

import com.joshualorett.runkit.location.Location
import com.joshualorett.runkit.location.LocationTracker
import com.joshualorett.runkit.time.TimeTracker
import kotlinx.coroutines.CoroutineDispatcher
import java.util.Date
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import toIsoString

/**
 * The main point of access to the active running session.
 * Created by Joshua on 12/29/2020.
 */
class ActiveSessionRepository(
    private val sessionDao: SessionDao,
    private val activeSessionDao: ActiveSessionDao,
    private val timeTracker: TimeTracker,
    private val locationTracker: LocationTracker,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default) {
    private var lastLocation: Location? = null
    val session: Flow<Session> = activeSessionDao.getActiveSessionFlow()

    suspend fun start() {
        var id = getCurrentSessionId()
        if (id == 0L) {
            id = sessionDao.createSession()
        }
        sessionDao.setSessionState(id, Session.State.STARTED)
        CoroutineScope(dispatcher).launch {
            timeTracker.start(session.first().elapsedTime)
            recordElapsedTime(coroutineContext)
        }
        CoroutineScope(dispatcher).launch {
            recordLocation(coroutineContext)
        }
    }

    suspend fun pause() {
        val id = getCurrentSessionId()
        sessionDao.setSessionState(id, Session.State.PAUSED)
        timeTracker.stop()
        lastLocation = null
    }

    suspend fun stop() {
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

    private suspend fun recordElapsedTime(
        coroutineContext: CoroutineContext,
        delayMs: Long = 1000
    ) = withContext(coroutineContext) {
        while (true) {
            delay(delayMs)
            ensureActive()
            val inSession = session.first().state == Session.State.STARTED
            if (!inSession) {
                cancel()
            } else {
                withContext(Dispatchers.Default) {
                    setElapsedTime(timeTracker.getElapsedTime())
                }
            }
        }
    }

    private suspend fun recordLocation(coroutineContext: CoroutineContext) =
        withContext(coroutineContext) {
            locationTracker.track().collect { location ->
                ensureActive()
                val inSession = session.first().state == Session.State.STARTED
                if (!inSession) {
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
