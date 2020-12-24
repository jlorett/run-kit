package com.joshualorett.fusedapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionService
import kotlinx.coroutines.flow.onEach

/**
 * [ViewModel] for Main view.
 * Created by Joshua on 9/27/2020.
 */
class MainViewModel: ViewModel() {
    var inSession = false
    lateinit var fusedLocationUpdateService: SessionService

    fun start() {
        try {
            fusedLocationUpdateService.start()
        } catch (e: SecurityException) {
            stop()
        }
    }

    fun pause() {
        fusedLocationUpdateService.pause()
    }

    fun stop() {
        fusedLocationUpdateService.stop()
    }

    fun observeElapsedTime(): LiveData<Long> {
        return fusedLocationUpdateService.elapsedTime
            .asLiveData()
    }

    fun observeSession(): LiveData<Session> {
        return fusedLocationUpdateService.session
            .onEach { inSession = it.state == Session.State.STARTED }
            .asLiveData()
    }
}