package com.joshualorett.fusedapp

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.joshualorett.fusedapp.database.SessionDatabase
import com.joshualorett.fusedapp.session.Session
import com.joshualorett.fusedapp.database.RoomSessionDaoDelegate
import com.joshualorett.fusedapp.database.active.RoomActiveSessionDaoDelegate
import com.joshualorett.fusedapp.math.calories.kilocaloriesExpended
import com.joshualorett.fusedapp.math.calories.metRunning
import com.joshualorett.fusedapp.session.FusedSessionService
import com.joshualorett.fusedapp.time.formatHoursMinutesSeconds
import com.joshualorett.fusedapp.time.formatMinutesSeconds
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val startSession = registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission: Boolean ->
        if (hasPermission) {
            viewModel.start()
        } else {
            showMessage("Location permission missing.")
        }
    }

    private val fusedServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: FusedSessionService.FusedLocationUpdateServiceBinder = service as FusedSessionService.FusedLocationUpdateServiceBinder
            viewModel.connectSessionService(binder.bindService(hasFineLocationPermission()))
        }

        override fun onServiceDisconnected(name: ComponentName) {
            viewModel.disconnectSessionService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!RoomSessionDaoDelegate.initialized || !RoomActiveSessionDaoDelegate.initialized) {
            val db = Room.databaseBuilder(
                applicationContext,
                SessionDatabase::class.java, "session"
            ).build()
            RoomSessionDaoDelegate.init(db.sessionDao(), db.locationDao())
            RoomActiveSessionDaoDelegate.init(db.activeSessionDao())
        }
        viewModel.session.observe(this@MainActivity, { session ->
            updateSessionUi(session)
        })
        actionBtn.setOnClickListener {
            val inSession = viewModel.inSession
            if (inSession) {
                viewModel.pause()
            } else {
                startSession.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        stopBtn.setOnClickListener {
            viewModel.stop()
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(Intent(this, FusedSessionService::class.java), fusedServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(fusedServiceConnection)
    }

    private fun showMessage(message: String) {
        Snackbar.make(actionBtn, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setDistance(distance: Float?) {
        this.distance.text = if(distance == null) "--" else formatDistance(distance)
    }

    private fun setAveragePace(averagePace: Double?) {
        this.avgPace.text = if(averagePace == null) "--" else
            "${formatMinutesSeconds(averagePace)} /km"
    }

    private fun setCalories(calories: Double?) {
        this.calories.text = if(calories == null) "--" else
            "${"%.2f".format(calories)} kcal"
    }

    private fun updateSessionUi(session: Session) {
        Log.d("logger", "Session: $session")
        time.text = formatHoursMinutesSeconds(session.elapsedTime.toDouble())
        when(session.state) {
            Session.State.STARTED -> {
                setDistance(session.distance)
                setAveragePace(session.averagePace())
                setCalories(kilocaloriesExpended(session.elapsedTime, 70.0, metRunning))
                actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_pause_24)
                actionBtn.text = getString(R.string.pause)
                stopBtn.hide()
            }
            Session.State.PAUSED -> {
                setDistance(session.distance)
                setAveragePace(session.averagePace())
                setCalories(kilocaloriesExpended(session.elapsedTime, 70.0, metRunning))
                actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_24)
                actionBtn.text = getString(R.string.resume)
                stopBtn.show()
            }
            Session.State.STOPPED -> {
                setDistance(null)
                setAveragePace(null)
                setCalories(null)
                actionBtn.icon = ContextCompat.getDrawable(this, R.drawable.ic_run_24)
                actionBtn.text = getString(R.string.start)
                stopBtn.hide()
            }
        }
    }
}
