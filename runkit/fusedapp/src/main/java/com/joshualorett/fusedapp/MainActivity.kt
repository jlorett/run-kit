package com.joshualorett.fusedapp

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationListener: FusedLocationObserver

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationListener = FusedLocationObserver(this, lifecycle) { locationData ->
            when(locationData) {
                is LocationData.Success -> updateLocationUi(locationData.location)
                is LocationData.Error.PermissionError -> {
                    setUiState(false)
                    showMessage(locationData.exception.toString())
                }
            }
        }
        fusedLocationListener.registerLifecycle(lifecycle)
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        actionBtn.setOnClickListener {
            val isRequestingUpdates = LocationUpdatePreferences.requestingLocationUpdates(this)
            if(isRequestingUpdates) {
                setUiState(false)
                fusedLocationListener.stopUpdates()
            } else {
                withPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    run = {
                        fusedLocationListener.startUpdates()
                        setUiState(true)
                    },
                    fallback = {
                        setUiState(false)
                        showMessage("Location permission missing.")
                    }
                )
            }
        }
        checkPermission()
    }

    override fun onDestroy() {
        fusedLocationListener.unregisterLifecycle(lifecycle)
        super.onDestroy()
    }

    private fun checkPermission() {
        val isRequestingUpdates = LocationUpdatePreferences.requestingLocationUpdates(this)
        if(isRequestingUpdates && !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationListener.stopUpdates()
            LocationUpdatePreferences.setRequestingLocationUpdates(this, false)
            setUiState(false)
            showMessage("Location permission missing.")
        } else {
            setUiState(LocationUpdatePreferences.requestingLocationUpdates(this))
        }
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
}
