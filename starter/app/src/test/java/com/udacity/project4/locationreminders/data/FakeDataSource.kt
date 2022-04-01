package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import java.lang.Error
import java.lang.Exception

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders:MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    //error flag
    private var shouldReturnError: Boolean = false

    fun setReturnError(value: Boolean){
        shouldReturnError = value
    }

//: Create a fake data source to act as a double to the real data source
    override suspend fun getReminders(): Result<List<ReminderDTO>> {

    if (shouldReturnError){
        return Result.Error("Test error - Not posible to load reminders.")
    }

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