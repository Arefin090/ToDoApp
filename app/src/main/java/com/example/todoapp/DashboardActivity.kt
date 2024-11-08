package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.ActionBarDrawerToggle

import com.google.android.material.navigation.NavigationView

class DashboardActivity : AppCompatActivity() {

    // Declare currentFragmentId to track the current fragment
    private var currentFragmentId: Int = R.id.menu_all

    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        // Set up the toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        // Set up the Drawer toggle (Hamburger menu)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    // Log out the user
                    auth.signOut()
                    // Redirect to LoginActivity
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish() // Close the dashboard activity
                    true
                }
                else -> false
            }
        }
        // Load the AllTasksFragment by default
        loadFragment(AllTasksFragment(), isForward = true)

        // Bottom Navigation View
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_all -> {
                    // Only switch fragment if it's different from the current fragment
                    if (currentFragmentId != R.id.menu_all) {
                        loadFragment(AllTasksFragment(), isForward = false)  // Backward navigation
                        currentFragmentId = R.id.menu_all
                    }
                    true
                }
                R.id.menu_completed -> {
                    // Load Completed Tasks Fragment
                    if (currentFragmentId != R.id.menu_completed) {
                        loadFragment(CompletedTasksFragment(), isForward = true)  // Forward navigation
                        currentFragmentId = R.id.menu_completed
                    }
                    true
                }
                R.id.menu_recurrent -> {
                    if (currentFragmentId != R.id.menu_recurrent) {
                        loadFragment(RecurrentFragment(), isForward = true)  // Forward navigation
                        currentFragmentId = R.id.menu_recurrent
                    }
                    true
                }
                else -> false
            }
        }
    }

    // Helper function to load a fragment with custom animations
    private fun loadFragment(fragment: Fragment, isForward: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()

        // Set custom animations based on whether navigation is forward or backward
        if (isForward) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,  // Enter animation
                R.anim.slide_out_left,  // Exit animation
                R.anim.slide_in_left,   // Pop Enter animation (when coming back)
                R.anim.slide_out_right  // Pop Exit animation (when leaving)
            )
        } else {
            transaction.setCustomAnimations(
                R.anim.slide_in_left,   // Enter animation for backward navigation
                R.anim.slide_out_right,  // Exit animation for backward navigation
                R.anim.slide_in_right,  // Pop Enter animation
                R.anim.slide_out_left   // Pop Exit animation
            )
        }

        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
