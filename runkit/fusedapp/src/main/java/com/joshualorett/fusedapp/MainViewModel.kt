package com.joshualorett.fusedapp

import androidx.lifecycle.*
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.FusedSessionRepository
import com.joshualorett.fusedapp.session.SessionService
import kotlinx.coroutines.flow.*

/**
 * [ViewModel] for Main view.
 * Created by Joshua on 9/27/2020.
 */
class MainViewModel(private var sessionRepository: FusedSessionRepository): ViewModel() {
    var inSession = false
    private val elapsedTime = sessionRepository.elapsedTime
    private val session = sessionRepository.session.onEach {
        inSession = it.state == Session.State.STARTED
    }

    fun start() {
        sessionRepository.start()
    }

    fun pause() {
        sessionRepository.pause()
    }

    fun stop() {
        sessionRepository.stop()
    }

    fun observeElapsedTime(): LiveData<Long> {
        return elapsedTime.asLiveData()
    }

    fun observeSession(): LiveData<Session> {
        return session.asLiveData()
    }

    fun connectSessionService(sessionService: SessionService) {
        sessionRepository.connectSessionService(sessionService)
    }

    fun disconnectSessionService() {
        sessionRepository.disconnectSessionService()
    }
}

class MainViewModelFactory(private val sessionRepository: FusedSessionRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(sessionRepository) as T
    }
}