package com.udacity.project4.locationreminders.data.local

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRuleInstrumented

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
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

    //---------------------------------------------------------------------------

    private lateinit var localDataSource: ReminderDataSource
    private lateinit var database: RemindersDatabase

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createRepository() {
        /*remindersFakeRepository = RemindersFakeRepository(remindersList)
        remindersFakeRepositoryEmpty = RemindersFakeRepository(remindersEmptyLIst)

        remindersLocalRepository = RemindersLocalRepository(
            remindersFakeRepository,
            Dispatchers.Unconfined)*/
        //When using real database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )

    }

    @After
    fun tearDownTests() = database.close()

    //Tests using fake data source

    //@Test
    fun getReminders_requestAllRemindersFromRemoteDataSource() = runBlockingTest {
        val reminders = remindersLocalRepository.getReminders() as Result.Success
        assertEquals(reminders.data, remindersList)
        //assertThat(reminders.data, IsEqual(remindersList))
    }

    //@Test
    fun saveReminder_requestAllRemindersFromRemoteDataSource() = runBlockingTest {
        remindersFakeRepository.saveReminder(reminder1)
        val reminders = remindersLocalRepository.getReminders() as Result.Success
        assertEquals(reminders.data.first(), reminder1)
        //assertThat(reminders.data, IsEqual(remindersList))
    }

    //@Test
    fun deleteAllReminders_requestAllRemindersFromRemoteDataSource() = runBlockingTest {
        remindersLocalRepository.deleteAllReminders()
        val reminders = remindersLocalRepository.getReminders() as Result.Success
        assertEquals(reminders.data.size, 0)
        //assertThat(reminders.data, IsEqual(remindersList))
    }

    //Tests using real DAO

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRuleInstrumented()

    @Test
    fun saveReminder_requestTheStoredReminderFromDataSource() = mainCoroutineRule.runBlockingTest {

        //Given a new local data source with on reminder
        mainCoroutineRule.pauseDispatcher()
        localDataSource.saveReminder(reminder1)

        //When we get that reminder from datasource
        val result = localDataSource.getReminder(reminder1.id) as Result.Success
        mainCoroutineRule.resumeDispatcher()

        //Then the recovered result should be the same as the inserted in the first step
        assertThat(result.data.id, `is`(reminder1.id))
    }

    @Test
    fun getReminders_requestAllRemindersStoredInRemoteDataSource() =
        mainCoroutineRule.runBlockingTest {
            //Given a local data source with reminders, three in this case
            mainCoroutineRule.pauseDispatcher()
            localDataSource.saveReminder(reminder1)
            localDataSource.saveReminder(reminder2)
            localDataSource.saveReminder(reminder3)

            //When we get all reminders
            val result = localDataSource.getReminders() as Result.Success
            val resultList = result.data

            //Then all the recovered results should be the same as the ones inserted in the beginning
            assertTrue(resultList.contains(reminder1))
            assertTrue(resultList.contains(reminder2))
            assertTrue(resultList.contains(reminder3))
        }

    @Test
    fun deleteAllReminders_RequesAllStoredRemindersFromRemoteDataBase() =
        mainCoroutineRule.runBlockingTest {
            //Given a local data source with reminders, three in this case
            mainCoroutineRule.pauseDispatcher()
            localDataSource.saveReminder(reminder1)
            localDataSource.saveReminder(reminder2)
            localDataSource.saveReminder(reminder3)

            //When we delete all reminders
            localDataSource.deleteAllReminders()
            val result = localDataSource.getReminders() as Result.Success
            val resulList = result.data

            //Then the recovered list of reminders must be empty
            assertEquals(resulList.size, 0)
        }

    @Test
    fun getReminder_requestAReminderThatDoesNotExist() = mainCoroutineRule.runBlockingTest {
        //Given a local data source with a reminder
        mainCoroutineRule.pauseDispatcher()
        localDataSource.saveReminder(reminder1)

        //When we ask for a reminder that does not exist in data base
        val result = localDataSource.getReminder(reminder2.id)
        mainCoroutineRule.pauseDispatcher()

        //Then the result must be an error
        val resultAsError = result as Result.Error
        assertThat(resultAsError.message, `is`("Reminder not found!"))
    }


}