package com.joshualorett.fusedapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionService
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * [ViewModel] for Main view.
 * Created by Joshua on 9/27/2020.
 */
class MainViewModel(private val fusedLocationUpdateService: SessionService): ViewModel() {
    var inSession = false

    fun start() {
        try {
            fusedLocationUpdateService.start()
        } catch (e: SecurityException) {
            stop()
        }
    }

    fun stop() {
        fusedLocationUpdateService.stop()
    }

    fun observeSession(): LiveData<Session> {
        return fusedLocationUpdateService.sessionFlow
            .onEach { inSession = it.state == Session.State.STARTED }
            .asLiveData()
    }

    fun checkSession(hasPermission: Boolean) {
        viewModelScope.launch {
            val inSession = fusedLocationUpdateService.inSession()
            val trackingLocation = fusedLocationUpdateService.trackingLocationFlow()
            //Tracking stopped, restarting location tracking.
            if (inSession && hasPermission && !trackingLocation) {
                try {
                    start()
                } catch (e: SecurityException) {
                    stop()
                }
            }
            //Permission lost, stopping location tracking.
            if (!hasPermission && inSession) {
                stop()
            }
        }
    }
}