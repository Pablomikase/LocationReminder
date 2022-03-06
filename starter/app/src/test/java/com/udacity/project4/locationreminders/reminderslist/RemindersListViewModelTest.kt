package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val myFakeDatabase = FakeDataSource(
        mutableListOf<ReminderDTO>(
            ReminderDTO(
                "Title test",
                "test description",
                "test location",
                123.123,
                321.321
            )
        )
    )

    private lateinit var remindersListViewModelTestEmpty: RemindersListViewModel
    private lateinit var remindersListViewModelTest: RemindersListViewModel

    //Tests configurations
    @Before
    fun setUpViewModels(){
        remindersListViewModelTestEmpty = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), FakeDataSource()
        )
        remindersListViewModelTest = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), myFakeDatabase
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // VIEWMODEL TESTS
    @Test
    fun invalidateShowNoData_remindersListIsNull() {
        // Given a fresh ListViewModel



        // When setting remindersList to null and load all the reminders list
        remindersListViewModelTestEmpty.remindersList.value = null
        remindersListViewModelTestEmpty.loadReminders()

        // Then showNoData should be true
        val value = remindersListViewModelTestEmpty.showNoData.getOrAwaitValue()
        assertEquals(value, true)

    }

    @Test
    fun invalidateShowNoData_remindersListIsEmpty() {
        // Given a fresh ListViewModel

        // When setting remindersList an empty list and load all the reminders list
        remindersListViewModelTestEmpty.remindersList.value = emptyList()
        remindersListViewModelTestEmpty.loadReminders()

        // Then showNoData should be true
        assertEquals(remindersListViewModelTestEmpty.showNoData.value, true)

    }

    @Test
    fun loadReminders_remindersListHasAtLeastOneReminder() {
        // Given a fresh ListViewModel

        // When loading one reminder from fake databse
        remindersListViewModelTest.loadReminders()

        // Then showNoData should Not be true and reminderList value should have more than one reminder
        assertEquals(remindersListViewModelTest.showNoData.value, false)
        assertTrue(remindersListViewModelTest.remindersList.value?.size!! > 0)

    }



    //LIVE DATA TESTING

    @Test
    fun reminderList_liveData(){
        //Given a fresh ListViewModel

        //When loading one reminder from fake database
        remindersListViewModelTest.loadReminders()

        //Then the LiveData with remindersList should have all the reminders
        val value = remindersListViewModelTest.remindersList.getOrAwaitValue()
        assertThat(value, (not(nullValue())))



    }


}