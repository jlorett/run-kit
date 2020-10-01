package com.joshualorett.fusedapp

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.joshualorett.fusedapp.session.SessionDataStore
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationListener: FusedLocationObserver
    private lateinit var viewModel: MainViewModel

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            SessionDataStore.init(applicationContext)
        }
        fusedLocationListener = FusedLocationObserver(this, lifecycle) { locationData ->
            when(locationData) {
                is LocationData.Success -> updateLocationUi(locationData.location)
                is LocationData.Error.PermissionError -> {
                    showMessage(locationData.exception.toString())
                }
            }
        }
        fusedLocationListener.registerLifecycle(lifecycle)
        viewModel = MainViewModel(SessionDataStore)
        viewModel.sessionLiveData.observe(this, { inSession ->
            setUiState(inSession)
        })
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        actionBtn.setOnClickListener {
            val inSession = viewModel.inSession
            if (inSession) {
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
        checkPermission()
    }

    override fun onDestroy() {
        fusedLocationListener.unregisterLifecycle(lifecycle)
        super.onDestroy()
    }

    private fun checkPermission() {
        if(viewModel.inSession && !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationListener.stopUpdates()
            showMessage("Location permission missing.")
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
