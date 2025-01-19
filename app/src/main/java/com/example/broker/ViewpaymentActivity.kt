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
import com.example.broker.UsersActivity
import com.example.broker.databinding.ActivityViewpaymentBinding
import com.google.android.material.navigation.NavigationView
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class ViewpaymentActivity : AppCompatActivity() {

    private val listofpaymentApi = "https://oxfpips.com/api/viewpay"
    private val updateUrl = "https://oxfpips.com/api/updateStatus"
    private lateinit var emailFromIntent: String
    private lateinit var binding: ActivityViewpaymentBinding
    private lateinit var recyclerView: RecyclerView
    private val paymentList = mutableListOf<Payment>()
    private lateinit var toggle: ActionBarDrawerToggle
    private val client = HttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewpaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.paymentRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        emailFromIntent = intent.getStringExtra("email") ?: ""

        if (emailFromIntent.isBlank()) {
            val goBackToLogin = Intent(this, LoginActivity::class.java)
            startActivity(goBackToLogin)
            finish()
        } else {
            setupNavigationView()

            // Access the NavigationView's header
            val navView: NavigationView = findViewById(R.id.navigationView)
            val headerView = navView.getHeaderView(0)  // Get the first header view
            val fullnameTextView: TextView = headerView.findViewById(R.id.fullnameTextView)

            // Set the email in the TextView
            fullnameTextView.text = emailFromIntent
            getInfo()
        }
    }

    private fun setupNavigationView() {
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    val goHome = Intent(this, MainActivity::class.java)
                    goHome.putExtra("email", emailFromIntent)
                    startActivity(goHome)
                }
                R.id.addpayment -> {
                    val goAddPayment = Intent(this, AddpaymentActivity::class.java)
                    goAddPayment.putExtra("email", emailFromIntent)
                    startActivity(goAddPayment)
                }
                R.id.users -> {
                    val goViewPayment = Intent(this, UsersActivity::class.java)
                    goViewPayment.putExtra("email", emailFromIntent)
                    startActivity(goViewPayment)
                }
                R.id.viewPayment -> {
                    Toast.makeText(this, "You're already viewing payments", Toast.LENGTH_SHORT).show()
                }
                R.id.logout -> {
                    val logoutIntent = Intent(this, LoginActivity::class.java)
                    startActivity(logoutIntent)
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun getInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(listofpaymentApi)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = false

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Reading the response on success
                    val reader = InputStreamReader(connection.inputStream)
                    val responseBody = reader.readText()
                    val jsonResponse = JSONObject(responseBody)
                    val jsonArray = jsonResponse.getJSONArray("paymentsystem")
                    paymentList.clear()

                    for (i in 0 until jsonArray.length()) {
                        val userObject = jsonArray.getJSONObject(i)
                        val productname = userObject.getString("productname")
                        val producttype = userObject.getString("producttype")
                        val address = userObject.getString("address")
                        val paymentId = userObject.getString("id") // This is used for the payment's id
                        val payment = Payment(paymentId, productname, producttype, address)
                        paymentList.add(payment)
                    }

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        if (paymentList.isEmpty()) {
                            Toast.makeText(this@ViewpaymentActivity, "No payment found", Toast.LENGTH_SHORT).show()
                        } else {
                            // Set up the adapter with updated payment list and onClick event for updating payment status
                            recyclerView.adapter = PaymentAddress(paymentList) { id ->
                                updatePaymentStatus(id) // Update payment when item is clicked
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Error", "Exception occurred: ${e.message}")
                    Toast.makeText(this@ViewpaymentActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updatePaymentStatus(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(updateUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true

                val postData = "id=$id"
                val output: OutputStream = connection.outputStream
                output.write(postData.toByteArray())

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        val index = paymentList.indexOfFirst { it.id == id }
                        if (index != -1) {
                            paymentList.removeAt(index)
                            recyclerView.adapter?.notifyItemRemoved(index)
                            showToast("Payment updated successfully")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Failed to update payment: $responseCode")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.message}")
                }
            }
        }
    }

    // Helper function for displaying Toasts
    private fun showToast(message: String) {
        Toast.makeText(this@ViewpaymentActivity, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
    }
}
