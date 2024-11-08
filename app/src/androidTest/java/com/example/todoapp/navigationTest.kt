package com.example.todoapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardNavigationTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(DashboardActivity::class.java)

    @Test
    fun testNavigationToAddTaskScreen() {
        // Perform click on the FAB (Floating Action Button) to add a new task
        onView(withId(R.id.fabAddTask)).perform(click())

        // Verify that the Add Task screen is displayed by checking if "Add Task" button is visible
        onView(withId(R.id.addTaskButton)).check(matches(isDisplayed()))
    }
}
