package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAndroidDataSource(var reminders:MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

//: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let {

            return Result.Success(it.toList())
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders.let {
            return Result.Success(reminders!!.toList()[id.toInt()])
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}