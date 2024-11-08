package com.example.todoapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Task data model
data class Task(
    var id: String = "",  // Default value to avoid null issues
    var title: String = "",
    var description: String = "",
    var isCompleted: Boolean = false,
    var userId: String = "",
    var deadline: Long? = null, //Deadline is nullable and will be stored as a timestamp
    var category: String = "personal",
    var priority: String = "Medium",
    var recurrence: String = "None",  // Recurrence field (None, Daily, Weekly, Monthly)
    var nextDueDate: Long? = null  // To track the next due date for recurring tasks
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", false, "", null, "personal", "Medium", "None", null)
}

// TaskAdapter for RecyclerView
class TaskAdapter(private var taskList: List<Task>, private val onTaskClicked: (Task) -> Unit) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder class to represent each task item
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        val taskDeadline: TextView = itemView.findViewById(R.id.taskDeadline) // Deadline TextView
        val checkBoxCompleted: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
    }

    // Inflating task layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    // Binding data to ViewHolder
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        // Set the correct priority circle
        val priorityDrawable = when (task.priority) {
            "Low" -> R.drawable.circle_green // For low priority tasks
            "Medium" -> R.drawable.circle_orange // For medium priority tasks
            "High" -> R.drawable.circle_red // For high priority tasks
            else -> null // No circle for tasks without a priority
        }

        // If a priorityDrawable is set, display it; otherwise, hide the ImageView
        if (priorityDrawable != null) {
            holder.itemView.findViewById<ImageView>(R.id.priorityCircle).apply {
                visibility = View.VISIBLE
                setImageResource(priorityDrawable)
            }
        } else {
            holder.itemView.findViewById<ImageView>(R.id.priorityCircle).visibility = View.GONE
        }

        holder.taskTitle.text = task.title
        holder.taskDescription.text = task.description
        holder.checkBoxCompleted.setOnCheckedChangeListener(null) // Prevents callback during recycling
        holder.checkBoxCompleted.isChecked = task.isCompleted
        // Display the deadline if available
        task.deadline?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            holder.taskDeadline.text = sdf.format(it)
        } ?: run {
            holder.taskDeadline.text = "No Deadline"
        }
        // Handle checkbox toggle
        holder.checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked

            // If the task is completed and it's recurring, reset it for the next cycle
            if (isChecked && task.recurrence != "None") {
                handleRecurringTask(task)
            }

            updateTaskInFirebase(task) // Update task in Firebase
        }

        // Handle task click to show task details in a dialog
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val taskDetailsDialog = AlertDialog.Builder(context)

            // Inflate custom layout for the dialog
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_task_details, null)
            taskDetailsDialog.setView(dialogView)
            val alertDialog = taskDetailsDialog.create()

            // Find and set values for the dialog views
            val taskTitleTextView: TextView = dialogView.findViewById(R.id.dialogTaskTitle)
            val taskDescriptionTextView: TextView = dialogView.findViewById(R.id.dialogTaskDescription)
            val taskPriorityTextView: TextView = dialogView.findViewById(R.id.dialogTaskPriority)
            val taskDeadlineTextView: TextView = dialogView.findViewById(R.id.dialogTaskDeadline)
            val taskCompletedCheckBox: CheckBox = dialogView.findViewById(R.id.dialogTaskCompletedCheckBox)

            // Set task details
            taskTitleTextView.text = task.title
            taskDescriptionTextView.text = task.description
            taskPriorityTextView.text = "Priority: ${task.priority}"
            taskDeadlineTextView.text = task.deadline?.let {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                sdf.format(it)
            } ?: "No Deadline"

            taskCompletedCheckBox.isChecked = task.isCompleted

            // Update completion status when checkbox is clicked
            taskCompletedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
                updateTaskInFirebase(task) // Update in Firebase
            }

            // Handle custom buttons directly from the dialog view
            val updateButton: MaterialButton = dialogView.findViewById(R.id.updateTaskButton)
            val deleteButton: MaterialButton = dialogView.findViewById(R.id.deleteTaskButton)
            val closeButton: MaterialButton = dialogView.findViewById(R.id.closeButton)

            updateButton.setOnClickListener {
                onTaskClicked(task) // Open the edit screen
                alertDialog.dismiss()  // Dismiss the dialog
            }

            deleteButton.setOnClickListener {
                deleteTaskFromFirebase(task)  // Call delete task function
                alertDialog.dismiss()  // Dismiss the dialog after deletion
            }

            closeButton.setOnClickListener {
                alertDialog.dismiss()  // Close the dialog
            }

            // Show the dialog
            alertDialog.show()
        }

        // Handle long click to delete task
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(it.context)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteTaskFromFirebase(task)
                }
                .setNegativeButton("No", null)
                .show()
            true
        }

    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    // Function to update task in Firebase
    private fun updateTaskInFirebase(task: Task) {
        val db = FirebaseFirestore.getInstance()
        db.collection("tasks").document(task.id)
            .set(task)
            .addOnSuccessListener {
                Log.d("TaskAdapter", "Task updated successfully")
            }
            .addOnFailureListener {
                Log.d("TaskAdapter", "Failed to update task")
            }
    }

    // Function to delete task from Firebase
    private fun deleteTaskFromFirebase(task: Task) {
        val db = FirebaseFirestore.getInstance()
        db.collection("tasks").document(task.id)
            .delete()
            .addOnSuccessListener {
                Log.d("TaskAdapter", "Task deleted successfully")
            }
            .addOnFailureListener {
                Log.d("TaskAdapter", "Failed to delete task")
            }
    }
    // Function to update task list with sorting by priority and deadline
    fun updateTaskList(newTaskList: List<Task>) {
        // Create DiffUtil callback
        val diffCallback = TaskDiffCallback(taskList, newTaskList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        // Sorting logic: First by priority, then by deadline
        taskList = newTaskList.sortedWith(compareBy<Task> { task ->
            // Sort by priority: High = 1, Medium = 2, Low = 3
            when (task.priority) {
                "High" -> 1
                "Medium" -> 2
                "Low" -> 3
                else -> 4 // Default case if priority is not set
            }
        }.thenBy { task ->
            // Sort by deadline (earliest first, null deadlines go to the bottom)
            task.deadline ?: Long.MAX_VALUE
        })

        Log.d("TaskAdapter", "Updating task list with refined sorting. New size: ${taskList.size}")
        diffResult.dispatchUpdatesTo(this)// Notify the adapter of changes using DiffUtil
    }

    private fun handleRecurringTask(task: Task) {
        val calendar = Calendar.getInstance()
        task.deadline?.let { calendar.timeInMillis = it }

        // Set the next due date based on recurrence
        when (task.recurrence) {
            "Daily" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "Monthly" -> calendar.add(Calendar.MONTH, 1)
        }

        // Update task with new deadline and reset the completion status
        task.deadline = calendar.timeInMillis
        task.isCompleted = false
        task.nextDueDate = task.deadline

        Log.d("TaskAdapter", "Recurring task updated: ${task.title}, next due date: ${task.deadline}")
    }


    class TaskDiffCallback(
        private val oldList: List<Task>,
        private val newList: List<Task>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Compare task IDs (assuming each task has a unique ID)
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Compare the content of the tasks
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

}