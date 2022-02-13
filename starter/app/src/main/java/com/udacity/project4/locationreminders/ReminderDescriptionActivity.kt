package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private var title = ""
    private var description = ""
    private var latitudeAndLongitude = ""

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
        private const val TAG = "ReminderDescription"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
//        : Add the implementation of the reminder details
            val selectedLocation = intent.getSerializableExtra(EXTRA_ReminderDataItem) as? ReminderDataItem
        catchValues(selectedLocation!!)
        setContentValues()
    }

    private fun catchValues(reminderDataItem: ReminderDataItem){
        title = reminderDataItem.title!!
        description = reminderDataItem.description!!
        latitudeAndLongitude = reminderDataItem.latitude.toString() + " & " + reminderDataItem.longitude.toString()
    }

    private fun setContentValues(){
        binding.title.text = title
        binding.description.text = description
        binding.latLngValues.text = latitudeAndLongitude
    }

}
