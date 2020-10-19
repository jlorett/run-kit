package com.joshualorett.fusedapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.joshualorett.fusedapp.distance.DistanceDao
import com.joshualorett.fusedapp.distance.formatDistance
import com.joshualorett.fusedapp.session.SessionDao
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * [ViewModel] for Main view.
 * Created by Joshua on 9/27/2020.
 */
class MainViewModel(sessionDao: SessionDao, distanceDao: DistanceDao): ViewModel() {
    var inSession = false
    val sessionLiveData: LiveData<Boolean> = sessionDao.getSessionFlow()
        .conflate()
        .onEach { inSession = it }
        .asLiveData()
    val distanceLiveData: LiveData<String> = distanceDao.getDistanceFlow()
        .map { distance ->
            if (!inSession) {
                "--"
            } else {
                formatDistance(distance)
            }
        }
        .conflate()
        .asLiveData()
}