package com.example.todoapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    // Reference to Firestore
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // FirebaseAuth to get the user
    private var selectedDeadline: Long? = null // Store the selected deadline
    private var isEditing = false  // Flag to check if we're editing
    private var taskId: String? = null  // Store task ID for editing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)

        // Set toolbar title based on editing or adding
        val bundle = intent.extras
        if (bundle != null) {
            isEditing = true
            taskId = bundle.getString("taskId")
            supportActionBar?.title = "Edit Task"
        } else {
            supportActionBar?.title = "Add Task"
        }

        // Handle back button click
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Reference UI elements
        val taskTitleEditText: EditText = findViewById(R.id.taskTitleEditText)
        val taskDescriptionEditText: EditText = findViewById(R.id.taskDescriptionEditText)
        val addTaskButton: Button = findViewById(R.id.addTaskButton)
        val selectDeadlineButton: TextView = findViewById(R.id.selectDeadlineButton) // Added this button for deadline
        val deadlineDisplay: TextView = findViewById(R.id.deadlineDisplay)
        val categoryChipGroup: ChipGroup = findViewById(R.id.categoryChipGroup)
        val priorityChipGroup: ChipGroup = findViewById(R.id.priorityChipGroup)
        val recurrenceChipGroup: ChipGroup = findViewById(R.id.recurrenceChipGroup)

        // Check if we're editing an existing task
        if (bundle != null) {
            isEditing = true
            taskId = bundle.getString("taskId")
            taskTitleEditText.setText(bundle.getString("taskTitle"))
            taskDescriptionEditText.setText(bundle.getString("taskDescription"))
            selectedDeadline = bundle.getLong("taskDeadline", 0)

            // Pre-select category, priority, and recurrence based on saved values
            preSelectChip(categoryChipGroup, bundle.getString("taskCategory", "personal"))
            preSelectChip(priorityChipGroup, bundle.getString("taskPriority", "Medium"))
            preSelectChip(recurrenceChipGroup, bundle.getString("taskRecurrence", "None"))

            addTaskButton.text = "Update Task"  // Change button text to "Update Task"
        }

        // Date and Time Picker for deadline
        selectDeadlineButton.setOnClickListener {
            pickDateAndTime(deadlineDisplay)  // Method to pick date and time
        }

        // Set up button listener to save or update the task
        addTaskButton.setOnClickListener {
            val title = taskTitleEditText.text.toString().trim()
            val description = taskDescriptionEditText.text.toString().trim()

            // Get selected category, priority, and recurrence from the ChipGroups
            val selectedCategory = getSelectedChipText(categoryChipGroup) ?: "personal"
            val selectedPriority = getSelectedChipText(priorityChipGroup) ?: "Medium"
            val selectedRecurrence = getSelectedChipText(recurrenceChipGroup) ?: "None"

            if (title.isNotEmpty() && description.isNotEmpty()) {
                // Get the current user ID
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    if (isEditing && taskId != null) {
                        // If editing, update the task
                        updateTaskInFirebase(
                            taskId!!,
                            title,
                            description,
                            userId,
                            selectedCategory,
                            selectedPriority,
                            selectedRecurrence
                        )
                    } else {
                        // If not editing, create a new task
                        createNewTaskInFirebase(
                            title,
                            description,
                            userId,
                            selectedCategory,
                            selectedPriority,
                            selectedRecurrence
                        )
                    }
                } else {
                    Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter both title and description", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper function to set the default selected chip in ChipGroup by text
    private fun setChipCheckedByText(chipGroup: ChipGroup, text: String?) {
        val chipCount = chipGroup.childCount
        for (i in 0 until chipCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.text == text) {
                chip.isChecked = true  // Set the chip as checked if it matches the text
                break
            }
        }
    }
    // Function to get selected chip text from a ChipGroup
    private fun getSelectedChipText(chipGroup: ChipGroup): String? {
        val selectedChipId = chipGroup.checkedChipId
        if (selectedChipId != ChipGroup.NO_ID) {
            val selectedChip: Chip = chipGroup.findViewById(selectedChipId)
            return selectedChip.text.toString()
        }
        return null
    }

    // Function to pre-select a chip in the ChipGroup when editing a task
    private fun preSelectChip(chipGroup: ChipGroup, value: String) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.text.toString().equals(value, ignoreCase = true)) {
                chip.isChecked = true
                break
            }
        }
    }

    // Function to pick the deadline using DatePicker and TimePicker
    private fun pickDateAndTime(deadlineDisplay: TextView) {
        val calendar = Calendar.getInstance()

        // Date Picker
        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // Time Picker
            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Save the selected deadline as timestamp
                selectedDeadline = calendar.timeInMillis


                // Format and display the selected date and time
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val formattedDate = sdf.format(calendar.time)

                // Update the TextView with the selected deadline
                deadlineDisplay.text = "Deadline: $formattedDate"  // Set the formatted date
                deadlineDisplay.visibility = TextView.VISIBLE  // Make the text visible

//                Toast.makeText(this, "Deadline set!", Toast.LENGTH_SHORT).show()

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

            timePicker.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.show()
    }

    // Function to create a new task in Firebase
    private fun createNewTaskInFirebase(
        title: String,
        description: String,
        userId: String,
        category: String,
        priority: String,
        recurrence: String
    ) {
        // Create a new task object
        val task = Task(
            id = db.collection("tasks").document().id, // Generate unique ID
            title = title,
            description = description,
            isCompleted = false,
            userId = userId, // Assign userId to the task
            deadline = selectedDeadline,
            category = category,
            priority = priority,
            recurrence = recurrence

        )
        Log.d("AddTaskActivity", "Creating new task: $title | Category: $category")  // Log task creation

        // Save the task to Firebase Firestore
        db.collection("tasks")
            .document(task.id)
            .set(task)
            .addOnSuccessListener {
                Log.d("AddTaskActivity", "Task successfully added")
                Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show()

                // Navigate back to DashboardActivity
                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clears all activities and starts a fresh DashboardActivity
                startActivity(intent)
            }
            .addOnFailureListener { error ->
                Log.e("AddTaskActivity", "Failed to add task: ${error.message}")
                Toast.makeText(this, "Failed to add task!", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to update an existing task in Firebase
    private fun updateTaskInFirebase(
        taskId: String,
        title: String,
        description: String,
        userId: String,
        category: String,
        priority: String,
        recurrence: String
    ) {
        // Update task object
        val updatedTask = Task(
            id = taskId,
            title = title,
            description = description,
            isCompleted = false,
            userId = userId,
            deadline = selectedDeadline,
            category = category,
            priority = priority,
            recurrence = recurrence
        )

        // Update the task in Firebase Firestore
        db.collection("tasks")
            .document(taskId)
            .set(updatedTask)
            .addOnSuccessListener {
                Toast.makeText(this, "Task updated successfully!", Toast.LENGTH_SHORT).show()

                // Navigate back to DashboardActivity
                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clears all activities and starts a fresh DashboardActivity
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update task!", Toast.LENGTH_SHORT).show()
            }
    }
}
