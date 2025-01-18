package com.example.broker

data class User(
    val name: String,
    val email: String,
    val profilePhotoUrl: String? = null // Optional field
)

