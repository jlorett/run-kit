package com.joshualorett.sample

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.joshualorett.runkit.session.Session
import com.joshualorett.runkit.session.SessionRepository
import com.joshualorett.sample.database.RoomSessionDao
import com.joshualorett.sample.database.SessionDatabase
import com.joshualorett.sample.database.SessionEntity
import com.joshualorett.sample.database.toSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Created by Joshua on 4/8/2021.
 */
class MainViewModel(private val sessionDao: RoomSessionDao): ViewModel() {
    val sessions = Pager(PagingConfig(pageSize = 4)) {
        sessionDao.getSessions()
    }
        .flow
        .map {
            it.map {
                sessionEntity: SessionEntity ->
                sessionEntity.toSession()
            }
        }
        .cachedIn(viewModelScope)

    class Factory(
        private val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                val sessionDao = SessionDatabase.getInstance(app.applicationContext).sessionDao()
                @Suppress("UNCHECKED_CAST") // Guaranteed to succeed at this point.
                return MainViewModel(sessionDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}