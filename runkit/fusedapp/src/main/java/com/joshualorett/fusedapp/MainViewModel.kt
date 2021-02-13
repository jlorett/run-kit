package com.joshualorett.fusedapp

import androidx.lifecycle.*
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * [ViewModel] for Main view.
 * Created by Joshua on 9/27/2020.
 */
class MainViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    private var sessionService: SessionService? = null
    var inSession = false
    private val _session = MutableStateFlow(getSavedSession())
    val session = _session.asStateFlow().asLiveData()

    fun start() {
        if(sessionService == null) {
            throw IllegalStateException("The SessionService must be connected first.")
        }
        try {
            sessionService?.start()
        } catch (e: SecurityException) {
            stop()
        }
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

    fun connectSessionService(sessionService: SessionService) {
        this.sessionService = sessionService
        viewModelScope.launch {
            sessionService.session.collect {
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
            savedStateHandle.set("date", date)
            savedStateHandle.set("distance", distance)
            savedStateHandle.set("elapsedTime", elapsedTime)
            savedStateHandle.set("state", state.toString())
        }
    }

    private fun getSavedSession(): Session {
        val id = savedStateHandle.get<Long>("id") ?: 0L
        val title = savedStateHandle.get<String>("title")
        val date = savedStateHandle.get<String>("date") ?:  Date().toIsoString()
        val elapsedTime = savedStateHandle.get<Long>("elapsedTime") ?: 0L
        val distance = savedStateHandle.get<Float>("distance") ?: 0F
        val state = Session.State.valueOf(savedStateHandle.get<String>("state") ?:
            Session.State.STOPPED.toString())
        return Session(id, title, date, elapsedTime, distance, state)
    }
}