package com.example.todoapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecurrentFragment : Fragment() {

    private lateinit var dailyTaskAdapter: TaskAdapter
    private lateinit var weeklyTaskAdapter: TaskAdapter
    private lateinit var monthlyTaskAdapter: TaskAdapter
    private val dailyTasks = mutableListOf<Task>()
    private val weeklyTasks = mutableListOf<Task>()
    private val monthlyTasks = mutableListOf<Task>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recurrent_tasks, container, false)

        // Initialize RecyclerViews for daily, weekly, and monthly tasks
        val recyclerViewDaily = view.findViewById<RecyclerView>(R.id.recyclerViewDailyTasks)
        recyclerViewDaily.layoutManager = LinearLayoutManager(context)
        dailyTaskAdapter = TaskAdapter(dailyTasks) { task -> /* Handle task click */ }
        recyclerViewDaily.adapter = dailyTaskAdapter

        val recyclerViewWeekly = view.findViewById<RecyclerView>(R.id.recyclerViewWeeklyTasks)
        recyclerViewWeekly.layoutManager = LinearLayoutManager(context)
        weeklyTaskAdapter = TaskAdapter(weeklyTasks) { task -> /* Handle task click */ }
        recyclerViewWeekly.adapter = weeklyTaskAdapter

        val recyclerViewMonthly = view.findViewById<RecyclerView>(R.id.recyclerViewMonthlyTasks)
        recyclerViewMonthly.layoutManager = LinearLayoutManager(context)
        monthlyTaskAdapter = TaskAdapter(monthlyTasks) { task -> /* Handle task click */ }
        recyclerViewMonthly.adapter = monthlyTaskAdapter

        // Fetch recurrent tasks from Firebase
        fetchRecurrentTasks()

        return view
    }

    private fun fetchRecurrentTasks() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereNotEqualTo("recurrence", "None")  // Fetch tasks that have recurrence
                .addSnapshotListener { snapshots, error ->
                    if (error != null || snapshots == null || snapshots.isEmpty) {
                        return@addSnapshotListener
                    }

                    dailyTasks.clear()
                    weeklyTasks.clear()
                    monthlyTasks.clear()

                    for (document in snapshots) {
                        val task = document.toObject(Task::class.java)
                        when (task.recurrence) {
                            "Daily" -> dailyTasks.add(task)
                            "Weekly" -> weeklyTasks.add(task)
                            "Monthly" -> monthlyTasks.add(task)
                        }
                    }

                    // Notify adapters about data changes
                    dailyTaskAdapter.notifyDataSetChanged()
                    weeklyTaskAdapter.notifyDataSetChanged()
                    monthlyTaskAdapter.notifyDataSetChanged()
                }
        } else {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }
}
