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
import com.example.broker.EditUserActivity
import com.example.broker.databinding.ActivityUsersBinding
import com.google.android.material.navigation.NavigationView
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class UsersActivity : AppCompatActivity() {

    private val getUserInformation = "https://oxfpips.com/api/api/user-information"
    private lateinit var binding: ActivityUsersBinding
    private val clientPayment = mutableListOf<UserInformation>()  // Initialize the list
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var recyclerView: RecyclerView
    private lateinit var emailFromIntent: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        emailFromIntent = intent.getStringExtra("email") ?: ""
        if (emailFromIntent.isBlank()) {
            val goBackToLogin = Intent(this@UsersActivity, LoginActivity::class.java)
            startActivity(goBackToLogin)
            finish() // It's good to call finish here to prevent returning to this activity on back press
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
                R.id.users->{
                    val goViewPayment = Intent(this, UsersActivity::class.java)
                    goViewPayment.putExtra("email",emailFromIntent)
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
        val client = HttpClient(CIO)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: HttpResponse = client.get(getUserInformation)
                val responseBody = response.bodyAsText()
                Log.d("Response", "Fetched data: $responseBody")

                withContext(Dispatchers.Main) {
                    if (response.status == HttpStatusCode.OK) {
                        try {
                            val jsonArray = JSONArray(responseBody)  // Parse as JSONArray instead of JSONObject

                            clientPayment.clear()  // Clear the list before adding new data

                            for (i in 0 until jsonArray.length()) {
                                val userObject = jsonArray.getJSONObject(i)  // Get each user object from the array
                                val name = userObject.getString("name")
                                val email = userObject.getString("email")
                                val profit = userObject.getString("profit")
                                val deposit = userObject.getString("deposit_amount")

                                // Add to the clientPayment list
                                clientPayment.add(UserInformation(name, email, profit, deposit))
                            }

                            // Notify the adapter if the list is updated
                            if (clientPayment.isNotEmpty()) {
                                val adapter = UsersPaymentAdapter(clientPayment)
                                recyclerView.adapter = adapter
                            } else {
                                Toast.makeText(this@UsersActivity, "No users found", Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: JSONException) {
                            Log.e("UsersActivity", "Error parsing JSON: ", e)
                            Toast.makeText(this@UsersActivity, "Error parsing data", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@UsersActivity, "Error: ${response.status}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("UsersActivity", "Error during fetching data: ", e)
                    Toast.makeText(this@UsersActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                client.close() // Always close the client
            }
        }
    }

}
