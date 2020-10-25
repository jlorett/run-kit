package com.joshualorett.fusedapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.joshualorett.fusedapp.session.Session
import kotlinx.coroutines.flow.onEach

/**
 * [ViewModel] for Main view.
 * Created by Joshua on 9/27/2020.
 */
class MainViewModel(private val fusedLocationObserver: FusedLocationObserver): ViewModel() {
    var inSession = false
    val sessionLiveData: LiveData<Session> = fusedLocationObserver.sessionFlow
        .onEach { inSession = it.state == Session.State.STARTED }
        .asLiveData()

    fun start() {
        try {
            fusedLocationObserver.startUpdates()
        } catch (e: SecurityException) {
            stop()
        }
    }

    fun stop() {
        fusedLocationObserver.stopUpdates()
    }
}