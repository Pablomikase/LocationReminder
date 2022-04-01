package com.udacity.project4.authentication

import android.content.ClipDescription
import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import java.util.concurrent.TimeUnit

/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> "Geofence error"
    }
}

/**
 * Stores latitude and longitude information along with a hint to help user find the location.
 */

internal object GeofencingConstants {

    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    val FAKE_DATA = arrayOf(
        ReminderDataItem(
            "casaBarcelona",
            "Prueba de desctipción",
            "Barcelona",
            41.367707,
            2.125527,
            "casa")
    )


    val NUM_LANDMARKS = FAKE_DATA.size
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
}
