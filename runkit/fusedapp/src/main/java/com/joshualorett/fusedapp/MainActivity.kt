package com.joshualorett.fusedapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var fusedLocationListener: FusedLocationObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationListener = FusedLocationObserver(this, lifecycle) { locationData ->
            when(locationData) {
                is LocationData.Success -> updateLocationUi(locationData.location)
                is LocationData.Error -> showMessage(locationData.exception.toString())
            }
        }
        fusedLocationListener.registerLifecycle(lifecycle)

        val isRequestingUpdates = LocationUpdatePreferences.requestingLocationUpdates(this)
        if(isRequestingUpdates) {
            withPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                run = {
                },
                fallback = {
                    fusedLocationListener.stopUpdates()
                    showMessage("Location permission missing.")
                })
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        LocationUpdatePreferences.getSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
        actionBtn.setOnClickListener {
            val isRequestingUpdates = LocationUpdatePreferences.requestingLocationUpdates(this)
            if(isRequestingUpdates) {
                fusedLocationListener.stopUpdates()
            } else {
               withPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    run = {
                        fusedLocationListener.startUpdates()
                    },
                    fallback = {
                        showMessage("Location permission missing.")
                    }
                )
            }
        }
        setUiState(LocationUpdatePreferences.requestingLocationUpdates(this))
    }

    override fun onStop() {
        LocationUpdatePreferences.getSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onDestroy() {
        fusedLocationListener.unregisterLifecycle(lifecycle)
        super.onDestroy()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateLocationUi(location: Location) {
        val dateFormat = SimpleDateFormat("HH:mm:ss.SSSZ", Locale.getDefault())
        this.time.text = dateFormat.format(Date(location.time))
        this.location.text = location.getLocationText()
    }

    private fun setUiState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            actionBtn.text = getString(R.string.stop)
        } else {
            location.text = "--"
            time.text = ""
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
