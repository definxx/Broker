package com.example.broker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.broker.databinding.ActivityEditUserBinding
import com.google.android.material.navigation.NavigationView
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.lifecycle.lifecycleScope
import com.example.broker.LoginActivity
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.setBody
import io.ktor.http.Url
import io.ktor.http.parameters
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class EditUserActivity : AppCompatActivity() {
    private val updateUserMoney = "https://oxfpips.com/api/updateuserprofite"

    private lateinit var binding: ActivityEditUserBinding
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var emailFromIntent: String
    private lateinit var clientEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        emailFromIntent = intent.getStringExtra("ownerEmail") ?: ""  // Owner's email
        clientEmail = intent.getStringExtra("clientEmail") ?: ""    // Client's email



        if (emailFromIntent.isBlank()) {
            val goBackToLogin = Intent(this@EditUserActivity, LoginActivity::class.java)
            startActivity(goBackToLogin)
        } else {
            clientEmail = intent.getStringExtra("clientEmail") ?: ""
            if (clientEmail.isNotEmpty()) {
                setupNavigation()

                // Access the NavigationView's header
                val navView: NavigationView = findViewById(R.id.navigationView)
                val headerView = navView.getHeaderView(0)  // Get the first header view
                val fullnameTextView: TextView = headerView.findViewById(R.id.fullnameTextView)

                // Set the email in the TextView
                fullnameTextView.text = emailFromIntent

                binding.addButton.setOnClickListener {
                    val clientProfit = binding.userProfit.text.toString().trim()
                    val clientDeposit = binding.userDeposit.text.toString().trim()
                    if (clientProfit.isNotEmpty() && clientDeposit.isNotEmpty()) {
                        updateClientAmount(clientProfit, clientDeposit)
                    } else {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateClientAmount(clientProfit: String, clientDeposit: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(updateUserMoney)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true // Enable output for sending body

                val postData = "clientEmail=$clientEmail&deposit=$clientDeposit&profit=$clientProfit&ownerEmail=$emailFromIntent"

                // Write the request body
                val outputStream: OutputStream = connection.outputStream
                outputStream.write(postData.toByteArray())

                val responseCode = connection.responseCode


                // Read the response
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }

                withContext(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        withContext(Dispatchers.Main) {
                            // Pass the email to the next activity
                            val goToMain = Intent(this@EditUserActivity, UsersActivity::class.java)
                            goToMain.putExtra("email", emailFromIntent)  // Passing email as an extra
                            startActivity(goToMain)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Exception: ${e.message}")
                    Log.e("EditUserActivity", "Error in updateClientAmount", e)
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@EditUserActivity, message, Toast.LENGTH_LONG).show()
    }

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
                    val logoutIntent = Intent(this@EditUserActivity, LoginActivity::class.java)
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
}
