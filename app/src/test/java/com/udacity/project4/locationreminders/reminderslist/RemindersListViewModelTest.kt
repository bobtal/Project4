package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.hamcrest.core.Is.`is`
import org.junit.Assert.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //Completed: provide testing to the RemindersListViewModel and its live data objects

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake repository to be injected into the view model.
    private lateinit var remindersRepository: FakeDataSource

    @Before
    fun setupViewModel() {
        stopKoin()
        // Initialise the repository with no reminders.
        remindersRepository = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), remindersRepository
        )
    }

    @Test
    fun loadReminders_loading() {
        // GIVEN - add three reminders to the viewmodel
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.remindersList.value = listOf<ReminderDataItem>(
            ReminderDataItem(
                "title1",
                "description1",
                "somewhere1",
                11.0,
                11.0,
                "random1"
            ),
            ReminderDataItem(
                "title2",
                "descriptio2n",
                "somewhere2",
                12.0,
                12.0,
                "random2"
            ),
            ReminderDataItem(
                "title3",
                "description3",
                "somewhere3",
                13.0,
                13.0,
                "random3"
            )
        )

        // WHEN - The reminders are loading
        remindersListViewModel.loadReminders()

        // THEN - The loaded data contains the expected number of values
        assertThat(remindersListViewModel.remindersList.value?.size, `is`(3))



    }


}