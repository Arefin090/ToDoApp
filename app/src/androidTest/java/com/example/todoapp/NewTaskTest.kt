package com.example.todoapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddTaskTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(DashboardActivity::class.java)

    @Test
    fun testAddNewTask() {
        // Navigate to the Add Task screen
        onView(withId(R.id.fabAddTask)).perform(click())

        // Enter the task title
        onView(withId(R.id.taskTitleEditText)).perform(typeText("Test Task"), closeSoftKeyboard())

        // Enter the task description
        onView(withId(R.id.taskDescriptionEditText)).perform(typeText("Test Description"), closeSoftKeyboard())

        // Click on Add Task button
        onView(withId(R.id.addTaskButton)).perform(click())

        // Verify that the task appears on the task list by checking the title
        onView(withText("Test Task")).check(matches(isDisplayed()))
    }
}
