package com.joshualorett.fusedapp.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.joshualorett.fusedapp.database.RoomSessionDao
import com.joshualorett.fusedapp.database.SessionEntity
import com.joshualorett.fusedapp.database.toSession
import kotlinx.coroutines.flow.map

class HomeViewModel(private val sessionDao: RoomSessionDao): ViewModel() {
    val sessions = Pager(PagingConfig(pageSize = 4)) {
        sessionDao.getPagedSessions()
    }.flow
        .map {
            it.map {
                    sessionEntity: SessionEntity ->
                sessionEntity.toSession()
            }
        }
        .cachedIn(viewModelScope)

    class Factory(
        private val sessionDao: RoomSessionDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(sessionDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}