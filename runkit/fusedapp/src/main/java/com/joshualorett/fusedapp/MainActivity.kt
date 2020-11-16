package com.joshualorett.fusedapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.session.SessionDataStore
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private var bound = false

    private val fusedServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: FusedSessionService.FusedLocationUpdateServiceBinder = service as FusedSessionService.FusedLocationUpdateServiceBinder
            viewModel = MainViewModel(binder.service)
            viewModel.observeSession().observe(this@MainActivity, { session ->
                updateSessionUi(session)
            })
            viewModel.checkSession(hasFineLocationPermission())
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!SessionDataStore.initialized) {
            SessionDataStore.init(applicationContext)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(Intent(this, FusedSessionService::class.java), fusedServiceConnection, Context.BIND_AUTO_CREATE)
        actionBtn.setOnClickListener {
            val inSession = viewModel.inSession
            if (inSession) {
                viewModel.pause()
            } else {
                withPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    run = {
                        viewModel.start()
                    },
                    fallback = {
                        showMessage("Location permission missing.")
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(fusedServiceConnection)
            bound = false
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(actionBtn, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setTime(time: Long?) {
        if (time == null) {
            this.time.text = "--:--:--"
        } else {
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            this.time.text = dateFormat.format(Date(time))
        }
    }

    private fun setDistance(distance: Float?) {
        this.distance.text = if(distance == null) "--" else formatDistance(distance)
    }

    private fun updateSessionUi(session: Session) {
        Log.d("logger", "Session: $session")
        when(session.state) {
            Session.State.STARTED -> {
                setTime(session.time)
                setDistance(session.distance)
                actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_pause_24)
                actionBtn.text = getString(R.string.pause)
            }
            Session.State.PAUSED -> {
                setTime(session.time)
                setDistance(session.distance)
                actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_24)
                actionBtn.text = getString(R.string.resume)
            }
            Session.State.STOPPED -> {
                setTime(null)
                setDistance(null)
                actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_run_24)
                actionBtn.text = getString(R.string.start)
            }
        }
    }
}
