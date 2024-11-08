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
class CompleteTaskTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(DashboardActivity::class.java)

    @Test
    fun testAddTaskButtonIsDisplayed() {
        // Check if the Add Task FloatingActionButton is displayed
        onView(withId(R.id.fabAddTask))
            .check(matches(isDisplayed()))
    }

}
