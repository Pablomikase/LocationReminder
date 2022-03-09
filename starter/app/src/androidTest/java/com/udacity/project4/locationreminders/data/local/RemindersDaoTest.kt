package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    val reminderForTest = ReminderDTO(
        "Test Name",
        "Test Description",
        "Test location ",
        123.123,
        321.321,
        "999"
    )

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun insertreminderAndGetById()= runBlockingTest {
        //GIVEN - insert a reminder
        database.reminderDao().saveReminder(reminderForTest)

        //WHEN - Get the reminder by id from the database
        val recoveredReminder = database.reminderDao().getReminderById(reminderForTest.id)
        //THEN - the loaded data contains the expected values
        assertThat<ReminderDTO>(recoveredReminder as ReminderDTO, notNullValue())
        assertThat(recoveredReminder.title, `is`(reminderForTest.title))
        assertThat(recoveredReminder.description, `is`(reminderForTest.description))
        assertThat(recoveredReminder.location, `is`(reminderForTest.location))
        assertThat(recoveredReminder.latitude, `is`(reminderForTest.latitude))
        assertThat(recoveredReminder.longitude, `is`(reminderForTest.longitude))
    }

    @Test
    fun clearingAllReminders()= runBlockingTest {
        //GIVEN - insert a reminder
        database.reminderDao().saveReminder(reminderForTest)

        //WHEN - Deleting all reminders
        database.reminderDao().deleteAllReminders()

        //THEN - the loaded data should be null
        val recoveredReminder = database.reminderDao().getReminderById(reminderForTest.id)
        assertThat(recoveredReminder, `is`(nullValue()))
    }

    @Test
    fun gettingAllReminders()= runBlockingTest {
        //GIVEN - insert a reminder
        database.reminderDao().saveReminder(reminderForTest)
        //WHEN - Get all the reminders from the database
        val remindersList = database.reminderDao().getReminders()

        //THEN - the loaded data contains the expected values
        assertThat<ReminderDTO>(remindersList.first() as ReminderDTO, notNullValue())
        assertThat(remindersList.first().title, `is`(reminderForTest.title))
        assertThat(remindersList.first().description, `is`(reminderForTest.description))
        assertThat(remindersList.first().location, `is`(reminderForTest.location))
        assertThat(remindersList.first().latitude, `is`(reminderForTest.latitude))
        assertThat(remindersList.first().longitude, `is`(reminderForTest.longitude))
    }

}