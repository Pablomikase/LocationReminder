package com.udacity.project4.locationreminders.savereminder

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.local.RemindersFakeRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
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


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest : AutoCloseKoinTest(){

    private lateinit var appContext: Application
    private lateinit var repository: ReminderDataSource

    val remindersForTest = mutableListOf(
        ReminderDTO(
            "Title test",
            "test description",
            "test location",
            123.123,
            321.321
        )
    )

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
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
            single { RemindersFakeRepository(remindersForTest) }
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

    //TOAST messages testing

    @Test
    fun savingAReminderWithNoTitle_showToastMessage() = runBlockingTest {
        //GIVEN - A saveReminder Fragment with an empty title
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withId(R.id.reminderDescription)).perform(typeText("test Description"), click(), closeSoftKeyboard())

        //WHEN - ReminderListFragment launched to display reminders
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN - page details are shown
        onView(withText(R.string.err_enter_title)).check(matches(isDisplayed()))
    }

    @Test
    fun savingAReminderWithNoDescription_showToastMessage() = runBlockingTest {
        //GIVEN - A saveReminder Fragment with an empty title
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withId(R.id.reminderTitle)).perform(typeText("test Description"), click(), closeSoftKeyboard())

        //WHEN - ReminderListFragment launched to display reminders
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN - page details are shown
        onView(withText(R.string.err_select_location)).check(matches(isDisplayed()))
    }

    //Using Idling resources

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource(){
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun savingAReminder_showToastMessage() = runBlockingTest {

        //GIVEN - A RemindersActivity
        //val scenario = launchFragmentInContainer<SelectLocationFragment>(Bundle(), R.style.AppTheme)
        val scenario = launchActivity<RemindersActivity>()
        //val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        val navController = mock(NavController::class.java)

        //WHEN - the user clicks on  the add-new-reminder button
        onView(withId(R.id.addReminderFAB)).perform(click())

        //AND - the user click on add-new-location
        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.map)).perform(longClick())

        //AND - When the location is selected I click on the save button
        onView(withId(R.id.saveSelectedLocationButton)).perform(click())

        //AND - I write a location name
        onView(withId(R.id.reminderTitle)).perform(typeText("reminder title"), click(), closeSoftKeyboard())

        //AND - I write a location description
        onView(withId(R.id.reminderDescription)).perform(typeText("test Description"), click(), closeSoftKeyboard())

        //AND - I click on the save reminder button
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN - I should see the reminders list fragment and a toast as a confirmation
        onView(withText(R.string.reminder_saved_and_geofence_entered)).inRoot(RootMatchers.withDecorView(
            CoreMatchers.not(getActivity(scenario)?.window?.decorView)
        ))

    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }


}