package com.joshualorett.fusedapp.active

import androidx.lifecycle.*
import com.joshualorett.fusedapp.toIsoString
import com.joshualorett.runkit.session.ActiveSessionService
import com.joshualorett.runkit.session.Session
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * [ViewModel] for the active session.
 * Created by Joshua on 9/27/2020.
 */
class ActiveSessionViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    private var sessionService: ActiveSessionService? = null
    var inSession = false
    private val _session = MutableStateFlow(getSavedSession())
    val session = _session.asStateFlow()

    fun start() {
        if(sessionService == null) {
            throw IllegalStateException("The SessionService must be connected first.")
        }
        sessionService?.start()
    }

    fun pause() {
        if(sessionService == null) {
            throw IllegalStateException("The SessionService must be connected first.")
        }
        sessionService?.pause()
    }

    fun stop() {
        if(sessionService == null) {
            throw IllegalStateException("The SessionService must be connected first.")
        }
        sessionService?.stop()
    }

    fun connectSessionService(sessionService: ActiveSessionService) {
        this.sessionService = sessionService
        viewModelScope.launch {
            sessionService.session().collect {
                inSession = it.state == Session.State.STARTED
                _session.value = it
                saveSession(it)
            }
        }
    }

    fun disconnectSessionService() {
        this.sessionService = null
    }

    private fun saveSession(session: Session) {
        session.run {
            savedStateHandle.set("id", id)
            savedStateHandle.set("title", title)
            savedStateHandle.set("startTime", startTime)
            savedStateHandle.set("endTime", endTime)
            savedStateHandle.set("distance", distance)
            savedStateHandle.set("elapsedTime", elapsedTime)
            savedStateHandle.set("state", state.toString())
        }
    }

    private fun getSavedSession(): Session {
        val id = savedStateHandle.get<Long>("id") ?: 0L
        val title = savedStateHandle.get<String>("title")
        val startTime = savedStateHandle.get<String>("startTime") ?:  Date().toIsoString()
        val endTime = savedStateHandle.get<String>("endTime")
        val elapsedTime = savedStateHandle.get<Long>("elapsedTime") ?: 0L
        val distance = savedStateHandle.get<Double>("distance") ?: 0.0
        val state = Session.State.valueOf(savedStateHandle.get<String>("state") ?:
            Session.State.STOPPED.toString())
        return Session(id, title, startTime, endTime, elapsedTime, distance, state)
    }
}