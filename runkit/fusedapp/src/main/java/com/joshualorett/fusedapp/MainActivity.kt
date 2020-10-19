package com.joshualorett.fusedapp

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.joshualorett.fusedapp.distance.DistanceDataStore
import com.joshualorett.fusedapp.session.SessionDataStore
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationObserver: FusedLocationObserver
    private lateinit var viewModel: MainViewModel

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!SessionDataStore.initialized) {
            SessionDataStore.init(applicationContext)
        }
        if(!DistanceDataStore.initialized) {
            DistanceDataStore.init(applicationContext)
        }
        fusedLocationObserver = FusedLocationObserver(this, lifecycle) { locationData ->
            when(locationData) {
                is LocationData.Success -> setTime(locationData.location)
                is LocationData.Error.PermissionError -> {
                    showMessage(locationData.exception.toString())
                }
            }
        }
        fusedLocationObserver.registerLifecycle(lifecycle)
        viewModel = MainViewModel(SessionDataStore, DistanceDataStore)
        viewModel.sessionLiveData.observe(this, { inSession ->
            setUiState(inSession)
        })
        viewModel.distanceLiveData.observe(this, { distance ->
            setDistance(distance)
        })
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        actionBtn.setOnClickListener {
            val inSession = viewModel.inSession
            if (inSession) {
                fusedLocationObserver.stopUpdates()
            } else {
                withPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    run = {
                        fusedLocationObserver.startUpdates()
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
        fusedLocationObserver.unregisterLifecycle(lifecycle)
        super.onDestroy()
    }

    private fun checkPermission() {
        if(viewModel.inSession && !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationObserver.stopUpdates()
            showMessage("Location permission missing.")
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(actionBtn, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setTime(location: Location) {
        val dateFormat = SimpleDateFormat("HH:mm:ss.SSSZ", Locale.getDefault())
        this.time.text = dateFormat.format(Date(location.time))
    }

    private fun setUiState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_stop_24)
            actionBtn.text = getString(R.string.stop)
        } else {
            actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_run_24)
            distance.text = "--"
            time.text = ""
            actionBtn.text = getString(R.string.start)
        }
    }

    private fun setDistance(distance: String) {
        this.distance.text = distance
    }
}
