package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.authentication.GeofencingConstants
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient

    private lateinit var reminderToSave: ReminderDataItem

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    private val TAG = "SaveReminderFragment"

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private lateinit var contxt: Context


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {



                val title = _viewModel.reminderTitle.value
                val description = _viewModel.reminderDescription.value
                val location = _viewModel.reminderSelectedLocationStr.value
                val latitude = _viewModel.latitude.value
                val longitude = _viewModel.longitude.value

                val reminder = ReminderDTO(title, description, location, latitude, longitude)
                reminderToSave =
                    ReminderDataItem(title, description, location, latitude, longitude)

            //verify if user has permissions
            if (backgroundAndForegroundLocationPermissionApproved()) {
                if (_viewModel.validateAndSaveReminder(reminderToSave)) {
                    //Reminder is OK

                    //ask for enable location
                    checkDeviceLocationSettings(reminderToSave)

                }
            } else {
                //If user doesn't have permissions
                Toast.makeText(
                    requireContext(),
                    R.string.permission_denied_explanation_in_save_reminder,
                    Toast.LENGTH_SHORT
                ).show()
                requestForegroundAndBackgroundLocationPermissions()
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // : Step 7 add code to check that the user turned on their device location and ask
        //  again if they did not
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == 0){
                checkDeviceLocationSettings(reminderToSave, false)
            }else{
                checkDeviceLocationSettings(reminderToSave, true)
            }
        }
    }

    suspend fun saveReminder(reminder: ReminderDTO) {
        val localDataBaseDao = LocalDB.createRemindersDao(requireContext())
        localDataBaseDao.saveReminder(reminder)
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contxt = context
    }

    //Geofence
    fun sendGeofence() {

        if (!_viewModel.isGeofenceActive()) return

        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId(reminderToSave.id)
            // Set the circular region of this geofence.
            .setCircularRegion(
                _viewModel.latitude.value!!,
                _viewModel.longitude.value!!,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            // Set the expiration duration of the geofence. This geofence gets
            // automatically removed after this period of time.
            .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()

        // First, remove any existing geofences that use our pending intent
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                // Add the new geofence request with the new geofence
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    return@addOnCompleteListener
                }

                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        // Geofences added.

                        Toast.makeText(
                            contxt, R.string.reminder_saved_and_geofence_entered,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        Log.e("Add Geofence", geofence.requestId)
                        // Tell the viewmodel that we've reached the end of the game and
                        // activated the last "geofence" --- by removing the Geofence.
                        //viewModel.geofenceActivated()
                    }
                    addOnFailureListener {
                        // Failed to add geofences.
                        Toast.makeText(
                            contxt, R.string.geofences_not_added,
                            Toast.LENGTH_SHORT
                        ).show()
                        if ((it.message != null)) {
                            Log.w(TAG, it.message!!)
                        }
                    }
                }
            }
        }

    }

    private fun checkDeviceLocationSettings(
        reminderToSave: ReminderDataItem,
        resolve: Boolean = true
    ) {
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
                    checkDeviceLocationSettings(reminderToSave)
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Everything ready to send a Geofence")
                //User has permissions and location is enabled
                //Sending Geofence
                try {
                    sendGeofence()
                } catch (error: IllegalArgumentException) {
                    Log.e(TAG, error.toString())
                }

                //Saving Reminder in database
                _viewModel.viewModelScope.launch {
                    _viewModel.saveReminder(reminderToSave)
                }

                //Clearing data for the next reminder
                _viewModel.onClear()

                //Showing toast feedback
                Toast.makeText(
                    requireContext(),
                    getString(R.string.reminder_saved_and_geofence_entered),
                    Toast.LENGTH_LONG
                ).show()

                //Navigating to the list fragment
                findNavController().navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
            }
        }
    }

    @TargetApi(29)
    private fun backgroundAndForegroundLocationPermissionApproved(): Boolean {
        //Asked for Android 9 and bellow
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this.requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))

        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this.requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return backgroundPermissionApproved && foregroundLocationApproved
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (backgroundAndForegroundLocationPermissionApproved())
            return
        //asked for Android 9 and bellow
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
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
            R.string.permission_denied_explanation_in_save_reminder,
            Snackbar.LENGTH_INDEFINITE
        )
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
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
            checkDeviceLocationSettings(reminderToSave)
        }


    }

    companion object {
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    }

}
