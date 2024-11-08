package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CompletedTasksFragment : Fragment() {

    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var isDataLoaded = false  // To prevent refetching data unnecessarily

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var noTasksTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_completed_tasks, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks)
        recyclerViewTasks.layoutManager = LinearLayoutManager(context)
        noTasksTextView = view.findViewById(R.id.noTasksTextView)

        // Initialize the task adapter with the update functionality
        taskAdapter = TaskAdapter(taskList) { task ->
            // Start AddTaskActivity for updating the task
            val intent = Intent(context, AddTaskActivity::class.java).apply {
                putExtra("taskId", task.id)
                putExtra("taskTitle", task.title)
                putExtra("taskDescription", task.description)
                putExtra("taskDeadline", task.deadline)
                putExtra("taskCategory", task.category)
                putExtra("taskPriority", task.priority)
                putExtra("taskRecurrence", task.recurrence)
            }
            startActivity(intent)
        }
        recyclerViewTasks.adapter = taskAdapter

        // Fetch completed tasks from Firebase
        fetchCompletedTasksFromFirebase()

        if (!isDataLoaded) {
            fetchCompletedTasksFromFirebase()
            isDataLoaded = true
        }


        return view
    }

    private fun fetchCompletedTasksFromFirebase() {

        progressBar.visibility = View.VISIBLE
        recyclerViewTasks.visibility = View.GONE
        noTasksTextView.visibility = View.GONE

        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("tasks")
                .whereEqualTo("userId", userId)  // Filter tasks by logged-in user's ID
                .whereEqualTo("completed", true)  // Fetch only completed tasks
                .addSnapshotListener { snapshots, error ->
                    progressBar.visibility = View.GONE
                    recyclerViewTasks.visibility = View.VISIBLE
                    noTasksTextView.visibility = View.GONE

                    if (error != null) {
                        Toast.makeText(context, "Error fetching tasks!", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    // Clear the current list and populate with new data
                    taskList.clear()
                    for (document in snapshots!!) {
                        val task = document.toObject(Task::class.java)
                        taskList.add(task)
                    }
                    // Sort tasks by deadline
                    taskList.sortBy { it.deadline ?: Long.MAX_VALUE }
                    taskAdapter.notifyDataSetChanged()
                }
        } else {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }
}
