package com.joshualorett.fusedapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * [ViewModel] for Main view.
 * Created by Joshua on 9/27/2020.
 */
class MainViewModel: ViewModel() {
    private var fusedLocationUpdateService: SessionService? = null
    private val elapsedTime = MutableStateFlow(0L)
    private val session = MutableStateFlow(Session())
    private var timerJob: Job? = null
    private var sessionJob: Job? = null
    var inSession = false

    fun start() {
        try {
            fusedLocationUpdateService?.start()
        } catch (e: SecurityException) {
            stop()
        }
    }

    fun pause() {
        fusedLocationUpdateService?.pause()
    }

    fun stop() {
        fusedLocationUpdateService?.stop()
    }

    fun observeElapsedTime(): LiveData<Long> {
        return elapsedTime.asLiveData()
    }

    fun observeSession(): LiveData<Session> {
        return session.asLiveData()
    }

    fun connectSessionService(sessionService: SessionService) {
        fusedLocationUpdateService = sessionService
        timerJob = viewModelScope.launch {
            fusedLocationUpdateService?.elapsedTime?.collect {
                elapsedTime.value = it
            }
        }
        sessionJob = viewModelScope.launch {
            fusedLocationUpdateService?.session?.collect {
                inSession = it.state == Session.State.STARTED
                session.value = it
            }
        }
    }

    fun disconnectSessionService() {
        sessionJob?.cancel()
        timerJob?.cancel()
    }
}