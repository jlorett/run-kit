package com.joshualorett.fusedapp

import android.Manifest
import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var receiver: FusedLocationUpdateReceiver
    private var locationUpdateService: FusedLocationUpdateService? = null
    private var bound = false

    // Monitors the state of the connection to the service.
    private val locationServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: FusedLocationUpdateService.LocalBinder = service as FusedLocationUpdateService.LocalBinder
            locationUpdateService = binder.service
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationUpdateService = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = FusedLocationUpdateReceiver()
        setContentView(R.layout.activity_main)

        val isRequestingUpdates = LocationUpdatePreferences.requestingLocationUpdates(this)
        if(isRequestingUpdates) {
            withPermission(Manifest.permission.ACCESS_FINE_LOCATION, run = {}, fallback = {
                locationUpdateService?.removeLocationUpdates()
                showMessage("Location permission missing.")
            })
        }
    }

    override fun onStart() {
        super.onStart()
        LocationUpdatePreferences.getSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
        actionBtn.setOnClickListener {
            val isRequestingUpdates = LocationUpdatePreferences.requestingLocationUpdates(this)
            if(isRequestingUpdates) {
                locationUpdateService?.removeLocationUpdates() ?: showMessage("Location service init error.")
            } else {
                withPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    run = {
                        locationUpdateService?.requestLocationUpdates() ?: showMessage("Location service init error.")
                    },
                    fallback = {
                        showMessage("Location permission missing.")
                    }
                )
            }
        }
        setUiState(LocationUpdatePreferences.requestingLocationUpdates(this))
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(Intent(this, FusedLocationUpdateService::class.java), locationServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(FusedLocationUpdateService.actionBroadcast))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onStop() {
        if (bound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(locationServiceConnection)
            bound = false
        }
        LocationUpdatePreferences.getSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    /**
     * Receiver for broadcasts sent by [FusedLocationUpdatesService].
     */
    private inner class FusedLocationUpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(FusedLocationUpdateService.extraLocation)
            if (location != null) {
                this@MainActivity.location.text = location.getLocationText()
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setUiState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            actionBtn.text = getString(R.string.stop)
        } else {
            location.text = "--"
            actionBtn.text = getString(R.string.start)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Update the buttons state depending on whether location updates are being requested.
        if (key == LocationUpdatePreferences.requestLocationUpdatesKey) {
            setUiState(LocationUpdatePreferences.requestingLocationUpdates(this))
        }
    }
}
