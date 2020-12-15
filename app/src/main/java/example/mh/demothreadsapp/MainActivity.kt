package example.mh.demothreadsapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

const val LOC_REQUEST_CODE = 80

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var saveLocation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        onStartButtonPressed()
    }

    private fun onStartButtonPressed(){
        start_button_id.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                Log.i("MAINLOG", "Operations starting...")

                for (i in 1..1000000) {
                    val location = async(IO) {
                        getLocation()
                    }
                    val battery = async(IO) {
                        getBatteryLevel()
                    }
                    Log.i("MAINLOG", "Result: ${location.await()}, battery: ${battery.await()}")
                }
            }
        }
    }

    private suspend fun getLocation() : String{
        delay(3000)
        return getCurrentLocationAsText()
    }

    private suspend fun getBatteryLevel() : String{
        delay(2000)
        return getBatteryPercentage()
    }

    private fun getBatteryPercentage() : String{
        val batteryManager : BatteryManager = this.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return batteryLevel.toString()
    }

    private fun getCurrentLocationAsText() : String{
        getCurrentLocation()
        val lat = lastLocation?.latitude.toString()
        val lon = lastLocation?.longitude.toString()
        saveLocation = "$lat, $lon"
        return saveLocation
    }

    private fun getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOC_REQUEST_CODE)
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        lastLocation = location
                    }
            }
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    lastLocation = location
                }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOC_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        lastLocation = location
                    }
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_LONG).show();
            }
        }
    }
}