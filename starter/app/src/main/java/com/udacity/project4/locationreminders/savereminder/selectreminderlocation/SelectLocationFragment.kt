package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.authentication.GeofencingConstants
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        //Testing values
        private var latitude = 37.42
        private var longitude = -122.0678
        private val zoomLevel = 15f

        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val LOCATION_PERMISSION_INDEX = 0

        internal const val ACTION_GEOFENCE_EVENT =
            "RemindersActivity.treasureHunt.action.ACTION_GEOFENCE_EVENT"
    }

    //Geofencing variables
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this.requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            this.requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var lastKnownLocation: Location

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private val authenticationviewModel by viewModels<AuthenticationViewModel>()
    private lateinit var binding: FragmentSelectLocationBinding

    //Maps variables
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val TAG = SelectLocationFragment::class.java.simpleName
    private lateinit var currentMarker: Marker


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        //Setting binding variables
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        //Setting geofence variables
        geofencingClient = LocationServices.getGeofencingClient(this.requireActivity())

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//         add the map setup implementation
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
//        : zoom to the user location after taking his permission
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.requireContext())

        resetLatAndLongValues()

//        : add style to the map
//        : put a marker to location that the user selected
//        : call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }

    override fun onStart() {
        checkPermissionsAndStartLocalization()
        super.onStart()
    }

    private fun resetLatAndLongValues() {
        _viewModel.latitude.value = null
        _viewModel.longitude.value = null
        _viewModel.reminderSelectedLocationStr.value = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        setMapStyle(map)
        //Location Variables
        val homeLatLng = LatLng(latitude, longitude)
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        setHomePosition()
        currentMarker = map.addMarker(MarkerOptions().position(homeLatLng))
        setMapLongClick(map)
        setPoiClick(map)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // : Step 7 add code to check that the user turned on their device location and ask
        //  again if they did not
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettings(false)
        }


    }

    private fun onLocationSelected() {
        //        : When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        binding.saveSelectedLocationButton.setOnClickListener {

            if (_viewModel.latitude.value == null ||
                _viewModel.latitude.value == 0.0 ||
                _viewModel.longitude.value == null ||
                _viewModel.longitude.value == 0.0
            ) {
                Toast.makeText(context, R.string.select_poi, Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            currentMarker.remove()
            currentMarker.remove()
            currentMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)

            )
            _viewModel.latitude.value = latLng.latitude
            _viewModel.longitude.value = latLng.longitude
            _viewModel.reminderSelectedLocationStr.value =
                String.format("%.3f", latLng.latitude) + " - " +
                        String.format("%.3f", latLng.longitude)
            Toast.makeText(
                this.requireContext(),
                R.string.user_feedback_after_selecting_poi,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            map.setMyLocationEnabled(true)
        } else {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun setHomePosition() {
        //mueve al usuario a su localizacion
        /*try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.result!!
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                lastKnownLocation.latitude,
                                lastKnownLocation.longitude
                            ), 15f
                        )
                    )
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    Log.e(TAG, "Exception: %s", task.exception)

                    map?.uiSettings?.isMyLocationButtonEnabled = false
                }
            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }*/

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude), 15f
                        )
                    )
                }
            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    //Geofences
    private fun checkPermissionsAndStartLocalization() {
        //if (viewModel.geofenceIsActive()) return
        if (foregroundLocationPermissionApproved()) {
            checkDeviceLocationSettings()
        } else {
            requestForegroundLocationPermissions()
        }
    }

    private fun foregroundLocationPermissionApproved(): Boolean {
        return (
                PackageManager.PERMISSION_GRANTED ==
                        checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
    }

    private fun requestForegroundLocationPermissions() {
        if (foregroundLocationPermissionApproved())
            return
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE

        Log.d(TAG, "Requesting foreground permissions")
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        Log.d(TAG, "onRequestPermissionResult")
        var snackbar: Snackbar = Snackbar.make(
            requireActivity().findViewById(R.id.nav_host_fragment),
            R.string.permission_denied_explanation,
            Snackbar.LENGTH_INDEFINITE
        )
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE)
        ) {
            snackbar
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {

            //Cuando el usuario tiene ya los permisos otorgados
            snackbar = Snackbar.make(
                requireActivity().findViewById(R.id.nav_host_fragment),
                R.string.loadingButtonText,
                Snackbar.LENGTH_SHORT
            )
            snackbar.show()
            enableMyLocation()
            checkDeviceLocationSettings()
            setHomePosition()
        }


    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        // :  add code to check that the device's location is on
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this.requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireActivity().findViewById(R.id.nav_host_fragment),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {

                Log.i(TAG, "Todo listo para seleccionar un punto en el mapa")
                //GeofenceTransitionsJobIntentService.enqueueWork(this.context!!, )
                //GeofenceTransitionsJobIntentService.enqueueWork(this, intent)
            }
        }

    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            currentMarker.remove()
            currentMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            currentMarker.showInfoWindow()
            _viewModel.latitude.value = poi.latLng.latitude
            _viewModel.longitude.value = poi.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = poi.name
        }
    }

}
