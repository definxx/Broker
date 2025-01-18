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
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val listOfUsersUrl = "https://oxfpips.com/api/listOfUsers"
    private lateinit var recyclerView: RecyclerView
    private val userList = mutableListOf<User>()
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
            val goBackToLogin = Intent(this@MainActivity, LoginActivity::class.java)
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
                    Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.addpayment->{
                    val goAddPayment = Intent(this, AddpaymentActivity::class.java)
                        goAddPayment.putExtra("email",emailFromIntent)
                    startActivity(goAddPayment)
                }

                R.id.viewPayment->{
                    val goViewPayment = Intent(this, ViewpaymentActivity::class.java)
                    goViewPayment.putExtra("email",emailFromIntent)
                    startActivity(goViewPayment)
                }
                R.id.users->{
                    val goViewPayment = Intent(this, UsersActivity::class.java)
                    goViewPayment.putExtra("email",emailFromIntent)
                    startActivity(goViewPayment)
                }

                R.id.logout -> {
                    val logoutIntent = Intent(this@MainActivity, LoginActivity::class.java)
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
                val response: HttpResponse = client.get(listOfUsersUrl)

                val responseBody = response.bodyAsText()
                Log.d("Response", "Fetched data: $responseBody")

                withContext(Dispatchers.Main) {
                    if (response.status == HttpStatusCode.OK) {
                        val jsonResponse = JSONObject(responseBody)
                        val jsonArray = jsonResponse.getJSONArray("data")
                        userList.clear()

                        for (i in 0 until jsonArray.length()) {
                            val userObject = jsonArray.getJSONObject(i)
                            val name = userObject.getString("name")
                            val email = userObject.getString("email")
                            val profilePhotoUrl = userObject.optString("profile_photo_url", null)

                            val user = User(name, email, profilePhotoUrl)
                            userList.add(user)
                        }

                        if (userList.isEmpty()) {
                            Toast.makeText(this@MainActivity, "No users found", Toast.LENGTH_SHORT).show()
                        }

                        // Set up the adapter with click listener
                        recyclerView.adapter = UserAdapter(userList) { email ->
                            // Navigate to the next activity and pass the email
                            val intent = Intent(this@MainActivity, EditUserActivity::class.java)
                            intent.putExtra("clientEmail", email)  // Pass the client email to the next activity
                            intent.putExtra("ownerEmail", emailFromIntent)  // Pass the owner email to the next activity
                            startActivity(intent)

                        }


                    } else {
                        Toast.makeText(this@MainActivity, " ${response.status}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                client.close()
            }
        }
    }
}
