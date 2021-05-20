package com.joshualorett.fusedapp

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.joshualorett.fusedapp.database.RoomSessionDaoDelegate
import com.joshualorett.fusedapp.database.SessionDatabaseFactory
import com.joshualorett.fusedapp.database.active.RoomActiveSessionDaoDelegate
import com.joshualorett.fusedapp.session.FusedSessionService
import com.joshualorett.fusedapp.time.formatHoursMinutesSeconds
import com.joshualorett.fusedapp.time.formatMinutesSeconds
import com.joshualorett.runkit.session.Session

/**
 * A simple [Fragment] subclass.
 * Use the [ActiveSessionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActiveSessionFragment : Fragment() {
    private lateinit var actionBtn: ExtendedFloatingActionButton
    private lateinit var stopBtn: FloatingActionButton
    private lateinit var avgPace: TextView
    private lateinit var calories: TextView
    private lateinit var distance: TextView
    private lateinit var time: TextView
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
            viewModel.connectSessionService(binder.bindService())
        }

        override fun onServiceDisconnected(name: ComponentName) {
            viewModel.disconnectSessionService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_session, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!RoomSessionDaoDelegate.initialized || !RoomActiveSessionDaoDelegate.initialized) {
            val db = SessionDatabaseFactory.getInstance(requireContext())
            RoomSessionDaoDelegate.init(db.sessionDao(), db.locationDao())
            RoomActiveSessionDaoDelegate.init(db.activeSessionDao())
        }
        viewModel.session.observe(viewLifecycleOwner, { session ->
            updateSessionUi(session)
        })
        actionBtn = view.findViewById(R.id.actionBtn)
        stopBtn = view.findViewById(R.id.stopBtn)
        avgPace = view.findViewById(R.id.avgPace)
        calories = view.findViewById(R.id.calories)
        distance = view.findViewById(R.id.distance)
        time = view.findViewById(R.id.time)
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
        requireActivity().bindService(
            Intent(activity, FusedSessionService::class.java),
            fusedServiceConnection,
            Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unbindService(fusedServiceConnection)
    }

    private fun showMessage(message: String) {
        Snackbar.make(actionBtn, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setDistance(distance: Double?) {
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
                setCalories(session.calories(70.0))
                actionBtn.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_pause_24)
                actionBtn.text = getString(R.string.pause)
                stopBtn.hide()
            }
            Session.State.PAUSED -> {
                setDistance(session.distance)
                setAveragePace(session.averagePace())
                setCalories(session.calories(70.0))
                actionBtn.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_play_arrow_24)
                actionBtn.text = getString(R.string.resume)
                stopBtn.show()
            }
            Session.State.STOPPED -> {
                setDistance(null)
                setAveragePace(null)
                setCalories(null)
                actionBtn.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_run_24)
                actionBtn.text = getString(R.string.start)
                stopBtn.hide()
            }
        }
    }
}