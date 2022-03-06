package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val testReminder = ReminderDataItem(
        "testTitle",
        "testDescription",
        "testLocation",
        123.123,
        321.321,
        "3"
    )

    val testReminderNullValues = ReminderDataItem(
        null,
        "testDescription",
         null,
        123.123,
        321.321,
        "3"
    )

    private lateinit var myFakeDataSource:FakeDataSource
    private lateinit var saveReminderViewModelEmpty:SaveReminderViewModel

    //VIEWMODEL TESTING

    @Before
    fun configureViewModels(){
        myFakeDataSource = FakeDataSource(
            mutableListOf<ReminderDTO>()
        )
        saveReminderViewModelEmpty = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            myFakeDataSource
        )
    }

    @After
    fun tearDown(){
        stopKoin()
    }


    @Test
    fun saveReminder_savingOneReminder() = runBlockingTest{
        // Given A Fresh viewModel with en empty database


        //When saving a reminder
        saveReminderViewModelEmpty.saveReminder(testReminder)


        //Then ShowLoading must be false, show a toast message
        saveReminderViewModelEmpty.apply {
            assertFalse(showLoading.value!!)
            assertEquals(showToast.value, app.getString(R.string.reminder_saved))
        }

    }

    @Test
    fun validateEnteredData_tittleNullOrEmty(){
        // Given A Fresh viewModel with en empty database

        //When passing a reminder with null or empty tittle

        //Then the reminder must be invalid
        assertFalse(saveReminderViewModelEmpty.validateEnteredData(testReminderNullValues))

    }

    @Test
    fun validateEnteredData_locationNullOrEmpty(){
        // Given A Fresh viewModel with en empty database

        //When passing a reminder with null or empty tittle

        //Then the reminder must be invalid
        assertFalse(saveReminderViewModelEmpty.validateEnteredData(testReminderNullValues))

    }





}