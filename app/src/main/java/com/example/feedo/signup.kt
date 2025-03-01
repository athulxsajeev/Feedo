package com.example.feedo

import okhttp3.*
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

data class SignupRequest(val name: String, val password: String, val email: String, val phone_no: String)
data class ResponseMessage(val message: String?, val error: String?)

val BACK = "https://f43jd2nv-5000.asse.devtunnels.ms/"

fun signupUser(name: String, password: String, email: String, phoneNo: String) {
    val client = OkHttpClient()
    val gson = Gson()

    // Prepare the request body
    val signupRequest = SignupRequest(name, password, email, phoneNo)
    val requestBody = RequestBody.create(
        "application/json".toMediaTypeOrNull(),
        gson.toJson(signupRequest)
    )

    // Create the POST request
    val request = Request.Builder()
        .url("https://f43jd2nv-5000.asse.devtunnels.ms/signup") // Replace with your Flask server URL
        .post(requestBody)
        .build()

    // Make the HTTP call
    // Make the HTTP call
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle network error
            println("Signup failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) {
                    println("Signup error: ${response.message}")
                    return
                }

                // Parse the response
                val responseMessage = gson.fromJson(response.body?.string(), ResponseMessage::class.java)
                println("Signup response: ${responseMessage.message ?: responseMessage.error}")
            }
        }
    })
}