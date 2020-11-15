package com.joshualorett.fusedapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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
                val state = session.state
                if (state == Session.State.IDLE) {
                    setUiState(false)
                } else if (state == Session.State.STARTED) {
                    setTime(session.time)
                    setUiState(session.state == Session.State.STARTED)
                    setDistance(formatDistance(session.distance))
                }
            })
            viewModel.checkSession(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
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
                viewModel.stop()
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

    private fun setTime(time: Long) {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        this.time.text = dateFormat.format(Date(time))
    }

    private fun setUiState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_stop_24)
            actionBtn.text = getString(R.string.stop)
        } else {
            actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_run_24)
            distance.text = "--"
            time.text = "--:--:--"
            actionBtn.text = getString(R.string.start)
        }
    }

    private fun setDistance(distance: String) {
        this.distance.text = distance
    }
}
