package com.joshualorett.fusedapp

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
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

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!SessionDataStore.initialized) {
            SessionDataStore.init(applicationContext)
        }
        val fusedLocationObserver = FusedLocationObserver(this, lifecycle)
        viewModel = MainViewModel(fusedLocationObserver)
        viewModel.sessionLiveData.observe(this, { session ->
            val state = session.state
            if (state == Session.State.IDLE) {
                setUiState(false)
                //setDistance("--")
            } else if (state == Session.State.STARTED) {
                setTime(session.time)
                setUiState(session.state == Session.State.STARTED)
                setDistance(formatDistance(session.distance))
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
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
        checkPermission()
    }

    private fun checkPermission() {
        if(viewModel.inSession && !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            viewModel.stop()
            showMessage("Location permission missing.")
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
