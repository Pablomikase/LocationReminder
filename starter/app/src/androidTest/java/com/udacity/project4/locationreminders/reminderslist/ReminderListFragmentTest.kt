package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {



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
            single { FakeAndroidDataSource(get()) as ReminderDataSource }
            single { remindersForTest }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {

        }
    }

    //    : test the displayed data on the UI.

    @Test
    fun noRemindersScreenDetails_DisplayedInUi() = runBlockingTest {
        //GIVEN - Reminders list is empty
        repository.deleteAllReminders()

        //WHEN - ReminderListFragment launched to display reminders
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //THEN - page details are shown
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun oneReminderOnScreenDetails_DisplayedInUi() = runBlockingTest {
        //GIVEN - Reminders has one reminder
        //repository.saveReminder(reminder = remindersForTest.first())

        //WHEN - ReminderListFragment launched to display reminders
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //THEN - page details are shown
        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(isDisplayed()))
        onView(withId(R.id.locationOnList)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText(remindersForTest.first().title)))
        onView(withId(R.id.description)).check(matches(withText(remindersForTest.first().description)))
        onView(withId(R.id.locationOnList)).check(matches(withText(remindersForTest.first().location)))
    }

    //    : test the navigation of the fragments.
    @Test
    fun clickOnAddNewReminder_navigateToNewReminderFragment() {
        //GIVEN - A fresh viewmodel with a list reminder fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When I click on the add new reminder fragment
        onView(withId(R.id.addReminderFAB)).perform(click())


        //Then the addNewReminderFragment should appear
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    //Navigation does not use navigation component at this point
    //@Test
    fun clickOnAReminderElement_navigateToReminderDetails() {
        //GIVEN - A fresh viewmodel with a list reminder fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When I click on a reminder
        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Title test")), click()
            )
        )


        //Then I should navigate to fragment details
        verify(navController).navigate(
            ReminderListFragmentDirections.actionReminderListFragmentToReminderDescriptionActivity()
        )
    }




}