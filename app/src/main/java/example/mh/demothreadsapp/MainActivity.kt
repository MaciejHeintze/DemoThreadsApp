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
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
const val LOC_REQUEST_CODE = 80
const val LOG_TAG = "MAIN_TAG"
const val A_SEC_DELAY = 5000L
const val B_SEC_DELAY = 3000L

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var saveLocation: String
    private var resultList = mutableListOf<String>()
    private val scope = CoroutineScope(IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        onStartButtonPressed()
        onStopButtonClicked()
    }

    private fun onStartButtonPressed(){
        start_button_id.setOnClickListener {
            it.isEnabled = false
            Log.i(LOG_TAG, "Operations starting...")
            scope.async {
                repeat(10000){
                    getLocationValue(getLocation())
                    getBatteryValue(getBatteryLevel())
                }
            }
        }
    }

    private suspend fun getLocationValue(locationData: String) {
        scope.launch {
            delay(1000)
            if(isActive) {
                resultList.add(locationData)
                Log.i(LOG_TAG, "Location added: $locationData")
            }
        }
    }

    private suspend fun getBatteryValue(batteryData: String) {
        scope.launch {
            delay(1000)
            if(isActive) {
                resultList.add(batteryData)
                Log.i(LOG_TAG, "Battery added: $batteryData %")
            }
        }
    }

    private suspend fun getLocation() : String{
        delay(A_SEC_DELAY)
        validateList()
        return getCurrentLocationAsText()
    }

    private suspend fun getBatteryLevel() : String{
        delay(B_SEC_DELAY)
        validateList()
        return getBatteryPercentage()
    }

    private fun sendDataToServer(){
        Log.i(LOG_TAG, "Send to http server message: ${resultList.joinToString()}")
        resultList.clear()
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
    override fun onStart() {
        super.onStart()
        locationRequest()
    }

    private fun locationRequest(){
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 60000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val mLocationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        lastLocation = location
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOC_REQUEST_CODE)
            } else {
                LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(mLocationRequest, mLocationCallback, null)
            }
        } else {
            LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(mLocationRequest, mLocationCallback, null)
        }

    }

    private fun onStopButtonClicked(){
        stop_button_id.setOnClickListener {
            scope.cancel()
            Log.i(LOG_TAG, "Operations stopped! List: $resultList")
            resultList.clear()
        }
    }

    private fun validateList(){
        if (resultList.size > 20) {
            scope.cancel()
            sendDataToServer()
        }
    }
}