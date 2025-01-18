package com.example.broker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.broker.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val apiUrl = "https://oxfpips.com/api/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the image with Glide
        Glide.with(this)
            .load(R.drawable.brokerlogo) // Use R.drawable if it's a local resource
            .circleCrop() // Makes the image round
            .into(binding.roundImage) // Ensure this ID matches your XML

        binding.loginButton.setOnClickListener {
            val email = binding.userEmail.text.toString().trim()
            val password = binding.userPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                makeApiCall(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun makeApiCall(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Setup URL connection
                val url = URL(apiUrl)

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true
                // Send data
                val postData = "email=$email&password=$password"
                val outputStream: OutputStream = connection.outputStream
                outputStream.write(postData.toByteArray())

                // Read response
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        // Pass the email to the next activity
                        val goToMain = Intent(this@LoginActivity, MainActivity::class.java)
                        goToMain.putExtra("email", email)  // Passing email as an extra
                        startActivity(goToMain)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed: $responseCode",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorText = binding.erroeText // Assuming "erroeText" is the correct ID in your layout
                    errorText.text = e.message // Displaying the error message in the TextView

                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
