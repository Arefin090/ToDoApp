package com.example.todoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddTaskActivityTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(AddTaskActivity::class.java)

    @Test
    fun checkAddTaskScreenElementsDisplayed() {
        // Check if Add Task screen title is displayed
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("Add Task"))))

        // Check if Task Title, Description inputs are displayed
        onView(withId(R.id.taskTitleEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.taskDescriptionEditText)).check(matches(isDisplayed()))

        // Check if "Add Task" button is displayed
        onView(withId(R.id.addTaskButton)).check(matches(isDisplayed()))
    }

}
