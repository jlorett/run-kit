package com.joshualorett.fusedapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.joshualorett.fusedapp.active.ActiveSessionViewModel
import com.joshualorett.fusedapp.database.RoomSessionDaoDelegate
import com.joshualorett.fusedapp.database.SessionDatabaseFactory
import com.joshualorett.fusedapp.database.active.RoomActiveSessionDaoDelegate
import com.joshualorett.fusedapp.session.FusedSessionService

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<ActiveSessionViewModel>()
    private val fusedServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: FusedSessionService.FusedLocationUpdateServiceBinder =
                service as FusedSessionService.FusedLocationUpdateServiceBinder
            viewModel.connectSessionService(binder.bindService())
        }

        override fun onServiceDisconnected(name: ComponentName) {
            viewModel.disconnectSessionService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!RoomSessionDaoDelegate.initialized || !RoomActiveSessionDaoDelegate.initialized) {
            val db = SessionDatabaseFactory.getInstance(this)
            RoomSessionDaoDelegate.init(db.sessionDao(), db.locationDao())
            RoomActiveSessionDaoDelegate.init(db.activeSessionDao())
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(
            Intent(this, FusedSessionService::class.java),
            fusedServiceConnection,
            Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(fusedServiceConnection)
    }
}
