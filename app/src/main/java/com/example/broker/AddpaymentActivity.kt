package com.example.broker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.broker.MainActivity
import com.example.broker.databinding.ActivityAddpaymentBinding
import com.google.android.material.navigation.NavigationView
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddpaymentActivity : AppCompatActivity() {
    private val uploadPaymentApi = "https://oxfpips.com/api/pay"
    private lateinit var emailFromIntent: String
    private lateinit var binding: ActivityAddpaymentBinding
    private lateinit var progressBar: ProgressBar
    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddpaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if email is passed from the previous activity
        emailFromIntent = intent.getStringExtra("email") ?: ""

        if (emailFromIntent.isBlank()) {
            val goBackToLogin = Intent(this@AddpaymentActivity, LoginActivity::class.java)
            startActivity(goBackToLogin)
            finish()
        } else {
            setupNavigationView()

            binding.uploadButton.setOnClickListener {
                uploadPay()
            }
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
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Set the item selection listener for the navigation menu
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    // Handle home item click (You can add functionality here)
                    val goHome = Intent(this, MainActivity::class.java)
                    goHome.putExtra("email",emailFromIntent)
                    startActivity(goHome)
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
                R.id.logout -> {
                    val logoutIntent = Intent(this, LoginActivity::class.java)
                    startActivity(logoutIntent)
                    finish()
                }
                R.id.users->{
                    val goViewPayment = Intent(this, UsersActivity::class.java)
                    goViewPayment.putExtra("email",emailFromIntent)
                    startActivity(goViewPayment)
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


    private fun uploadPay() {
        val productName = findViewById<EditText>(R.id.productNameEditText).text.toString()
        val productType = findViewById<EditText>(R.id.productTypeEditText).text.toString()
        val address = findViewById<EditText>(R.id.addressEditText).text.toString()
        progressBar = findViewById(R.id.progressBar)

        if (productName.isBlank() || productType.isBlank() || address.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE

        val client = HttpClient(CIO)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.post(uploadPaymentApi) {
                    contentType(ContentType.Application.Json)
                    parameter("productname", productName)
                    parameter("producttype", productType)
                    parameter("address", address)
                    parameter("useremail", emailFromIntent)
                }

                withContext(Dispatchers.Main) {
                    progressBar.visibility = ProgressBar.GONE
                    if (response.status.value == 200) {
                        val productName = findViewById<EditText>(R.id.productNameEditText).text.clear()
                        val productType = findViewById<EditText>(R.id.productTypeEditText).text.clear()
                        val address = findViewById<EditText>(R.id.addressEditText).text.clear()
                        Toast.makeText(this@AddpaymentActivity, "Payment Uploaded", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorResponse = response.bodyAsText()
                        Toast.makeText(this@AddpaymentActivity, "$errorResponse", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this@AddpaymentActivity, "Payment Uploaded", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
