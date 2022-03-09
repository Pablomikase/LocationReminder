package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    : Add testing implementation to the RemindersLocalRepository.kt

    private val reminder1 =
        ReminderDTO("Title rem1", "Desc rem1", "location rem 1", 123.123, 321.321)
    private val reminder2 = ReminderDTO("Title rem2", "Desc rem2", "location rem 2", 13.123, 4.321)
    private val reminder3 =
        ReminderDTO("Title rem3", "Desc rem3", "location rem 3", 453.123, 341.321)

    private val remindersList = mutableListOf(
        reminder1, reminder2, reminder3
    )
    private val remindersEmptyLIst = mutableListOf<ReminderDTO>()


    private lateinit var remindersFakeRepository: RemindersFakeRepository
    private lateinit var remindersFakeRepositoryEmpty: RemindersFakeRepository

    //Class under test
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun createRepository() {

        remindersFakeRepository = RemindersFakeRepository(remindersList)
        remindersFakeRepositoryEmpty = RemindersFakeRepository(remindersEmptyLIst)

        remindersLocalRepository = RemindersLocalRepository(
            remindersFakeRepository,
            Dispatchers.Unconfined

        )
    }

    @Test
    fun getReminders_requestAllRemindersFromRemoteDataSource() = runBlockingTest {
        val reminders = remindersLocalRepository.getReminders() as Result.Success
        assertEquals(reminders.data, remindersList)
        //assertThat(reminders.data, IsEqual(remindersList))
    }

    @Test
    fun saveReminder_requestAllRemindersFromRemoteDataSource() = runBlockingTest {
        remindersFakeRepository.saveReminder(reminder1)
        val reminders = remindersLocalRepository.getReminders() as Result.Success
        assertEquals(reminders.data.first(), reminder1)
        //assertThat(reminders.data, IsEqual(remindersList))
    }

    @Test
    fun deleteAllReminders_requestAllRemindersFromRemoteDataSource() = runBlockingTest {
        remindersLocalRepository.deleteAllReminders()
        val reminders = remindersLocalRepository.getReminders() as Result.Success
        assertEquals(reminders.data.size, 0)
        //assertThat(reminders.data, IsEqual(remindersList))
    }

}