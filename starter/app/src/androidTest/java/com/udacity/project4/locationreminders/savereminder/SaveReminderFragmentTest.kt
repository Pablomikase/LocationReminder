package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.local.RemindersFakeRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
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

    @Test
    fun savingAReminderWithNoTitle_showToastMessage() = runBlockingTest {
        //GIVEN - A saveReminder Fragment with an empty title
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
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
        onView(withId(R.id.reminderTitle)).perform(typeText("test Description"), click(), closeSoftKeyboard())

        //WHEN - ReminderListFragment launched to display reminders
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN - page details are shown
        onView(withText(R.string.err_select_location)).check(matches(isDisplayed()))

    }
}