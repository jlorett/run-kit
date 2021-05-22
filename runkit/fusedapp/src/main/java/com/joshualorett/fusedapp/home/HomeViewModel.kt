package com.joshualorett.fusedapp.home

import android.os.Build
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
import com.joshualorett.runkit.session.Session
import kotlinx.coroutines.flow.map
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeViewModel(private val sessionDao: RoomSessionDao): ViewModel() {
    val sessions = Pager(PagingConfig(pageSize = 4)) {
        sessionDao.getPagedSessions()
    }.flow
        .map {
            it.map {
                    sessionEntity: SessionEntity ->
                val session = sessionEntity.toSession()
                var title = ""
                if(session.title == null) {
                    val startHour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDateTime.parse(session.startTime, DateTimeFormatter.ISO_DATE_TIME).hour
                    } else {
                        Date.valueOf(session.startTime).hours
                    }
                    title = when (startHour) {
                        in 0..11 -> "Morning Run"
                        in 12..15 -> "Afternoon Run"
                        in 16..20 -> "Evening Run"
                        in 21..23 -> "Night Run"
                        else -> "Session Run"
                    }
                }
                val endTime = session.endTime?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDateTime.parse(session.endTime, DateTimeFormatter.ISO_DATE_TIME)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    } else {
                        val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd")
                        dateTimeFormatter.format(Date.valueOf(session.endTime))
                    }
                } ?: session.endTime
                Session(
                    session.id,
                    title,
                    session.startTime,
                    endTime,
                    session.elapsedTime,
                    session.distance
                )
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