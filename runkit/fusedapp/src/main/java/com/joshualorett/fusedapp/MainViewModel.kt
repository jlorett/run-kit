package com.joshualorett.fusedapp

import androidx.lifecycle.*
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * [ViewModel] for Main view.
 * Created by Joshua on 9/27/2020.
 */
class MainViewModel: ViewModel() {
    private var sessionService: SessionService? = null
    var inSession = false
    private val _session = MutableStateFlow(Session())
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
            }
        }
    }

    fun disconnectSessionService() {
        this.sessionService = null
    }
}