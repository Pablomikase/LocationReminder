package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDaoTest
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest:AutoCloseKoinTest() {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    private lateinit var remindersListViewModelTestEmpty: RemindersListViewModel
    private lateinit var remindersListViewModelTest: RemindersListViewModel

    val myFakeDatabase = FakeAndroidDataSource(
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

    val remindersForTest = mutableListOf(
        ReminderDTO(
            "Title test",
            "test description",
            "test location",
            123.123,
            321.321
        )
    )

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { FakeAndroidDataSource(get()) as ReminderDataSource}
            single { RemindersDaoTest(remindersForTest) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /*@Before
    fun setUpViewModels() {
       remindersListViewModelTestEmpty = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), FakeAndroidDataSource()
        )
        remindersListViewModelTest = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), myFakeDatabase
        )
    }*/

    @Test
    fun noRemindersScreenDetails_DisplayedInUi() = runBlockingTest {
        //GIVEN - Reminders list is empty

        //WHEN - ReminderListFragment launched to display reminders
        var myScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)


        Thread.sleep(5000)

        //THEN - page details are shown
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

}