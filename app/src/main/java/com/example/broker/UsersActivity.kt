package com.example.broker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UsersActivity  : AppCompatActivity() {

    private val listOfUsersUrl = "https://oxfpips.com/api/user-information"
    private lateinit var recyclerView: RecyclerView
    private val userInformation = mutableListOf<UserInformation>()
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var emailFromIntent: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Check if email is passed from previous activity
        emailFromIntent = intent.getStringExtra("email") ?: ""
        if (emailFromIntent.isBlank()) {
            val goBackToLogin = Intent(this@UsersActivity , LoginActivity::class.java)
            startActivity(goBackToLogin)
        } else {
            getInfo()
            setupNavigation()
            // Access the NavigationView's header
            val navView: NavigationView = findViewById(R.id.navigationView)
            val headerView = navView.getHeaderView(0)  // Get the first header view
            val fullnameTextView: TextView = headerView.findViewById(R.id.fullnameTextView)

            // Set the email in the TextView
            fullnameTextView.text = emailFromIntent
        }
    }

    // Set up the Navigation Drawer
    private fun setupNavigation() {
        val navView: NavigationView = findViewById(R.id.navigationView)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerlayout)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.open_app,
            R.string.close_app
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Set the item selection listener for the navigation menu
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    // Handle home item click (You can add functionality here)
                    val goHome = Intent(this, MainActivity::class.java)
                    goHome.putExtra("email", emailFromIntent)
                    startActivity(goHome)
                }
                R.id.addpayment -> {
                    val goAddPayment = Intent(this, AddpaymentActivity::class.java)
                    goAddPayment.putExtra("email", emailFromIntent)
                    startActivity(goAddPayment)
                }

                R.id.viewPayment -> {
                    val goViewPayment = Intent(this, ViewpaymentActivity::class.java)
                    goViewPayment.putExtra("email", emailFromIntent)
                    startActivity(goViewPayment)
                }
                R.id.users -> {
                    val goViewPayment = Intent(this, UsersActivity::class.java)
                    goViewPayment.putExtra("email", emailFromIntent)
                    startActivity(goViewPayment)
                }

                R.id.logout -> {
                    val logoutIntent = Intent(this@UsersActivity, LoginActivity::class.java)
                    startActivity(logoutIntent)
                    finish()
                }
                else -> {
                    // Handle any other items if necessary
                }
            }
            // Close the drawer after an item is selected
            drawerLayout.closeDrawers()
            true
        }
    }

    // Fetch user data from the server
    private fun getInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(listOfUsersUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"  // Make sure the server supports GET
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = false  // Set to false for GET requests

                // Handling response codes properly
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Reading the response on success
                    val reader = InputStreamReader(connection.inputStream)
                    val responseBody = reader.readText()
                    val jsonResponse = JSONObject(responseBody)
                    val jsonArray = jsonResponse.getJSONArray("data")
                    userInformation.clear()

                    for (i in 0 until jsonArray.length()) {
                        val userObject = jsonArray.getJSONObject(i)
                        val name = userObject.getString("name")
                        val email = userObject.getString("email")
                        val deposit = userObject.getString("deposit_amount")
                        val profit = userObject.getString("profit")


                        val user = UserInformation(name, email, deposit,profit)
                        userInformation.add(user)
                    }

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        if (userInformation.isEmpty()) {
                            Toast.makeText(this@UsersActivity, "No users found", Toast.LENGTH_SHORT).show()
                        } else {
                            recyclerView.adapter = UsersPaymentAdapter(userInformation)
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle any exceptions

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UsersActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
