package com.joshualorett.fusedapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                showMessage("Permission granted.")
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    Log.d("log", "Location: $location")
                }
            } else {
                showMessage("Permission denied.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
