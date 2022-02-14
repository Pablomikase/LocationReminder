package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

/**
 * The Room Database that contains the reminders table.
 */
@Database(entities = [ReminderDTO::class], version = 1, exportSchema = false)
abstract class RemindersDatabase : RoomDatabase() {

    abstract fun reminderDao(): RemindersDao
}

private lateinit var INSTANCE: RemindersDatabase

fun getDataBase(context: Context) : RemindersDatabase{
    synchronized(RemindersDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                RemindersDatabase::class.java,
                "locationReminders.db").build()
        }
    }
    return INSTANCE
}