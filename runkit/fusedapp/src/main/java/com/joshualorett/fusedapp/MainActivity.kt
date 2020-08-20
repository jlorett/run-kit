package com.joshualorett.fusedapp

import android.content.*
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var fusedLocationListener: FusedLocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationListener = FusedLocationListener(this, lifecycle) { locationData ->
            when(locationData) {
                is LocationData.Success -> updateLocationUi(locationData.location)
                is LocationData.Error -> showMessage(locationData.exception.toString())
            }
        }
        lifecycle.addObserver(fusedLocationListener)
    }

    override fun onStart() {
        super.onStart()
        LocationUpdatePreferences.getSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
        actionBtn.setOnClickListener {
            val isRequestingUpdates = LocationUpdatePreferences.requestingLocationUpdates(this)
            if(isRequestingUpdates) {
                fusedLocationListener.stopUpdates()
            } else {
                fusedLocationListener.startUpdates()
            }
        }
        setUiState(LocationUpdatePreferences.requestingLocationUpdates(this))
    }

    override fun onStop() {
        LocationUpdatePreferences.getSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateLocationUi(location: Location) {
        this@MainActivity.location.text = location.getLocationText()
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
