package com.example.feedo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

data class ResponseMessageA(val message: String?, val error: String?, val name: String?, val mobile:String?)

data class SigninRequest(val email: String, val password: String)
val name = mutableStateOf("")
val mobile = mutableStateOf("")

fun signinUserBackend(email: String, password: String, loginError: MutableState<String>, onSuccess: MutableState<Boolean>) {
    val client = OkHttpClient()
    val gson = Gson()

    // Prepare the request body
    val signinRequest = SigninRequest(email, password)
    val requestBody = gson.toJson(signinRequest)
        .toRequestBody("application/json".toMediaTypeOrNull())

    // Create the POST request
    val request = Request.Builder()
        .url("https://f43jd2nv-5000.asse.devtunnels.ms/signin") // Replace with your Flask server URL
        .post(requestBody)
        .build()

    // Make the HTTP call
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle network error


            loginError.value = e.message.toString()
            println("Signin failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {

                println(response)
                println(response.message)
                if (!response.isSuccessful) {
                    println("Signin error: ${response.message}")
                    loginError.value = "Sign in failed"
                    onSuccess.value = true
                    return
                }

//                if (response.isSuccessful){
//                    loginError.value = "signin sucess"
//                    println(response.message)
                //    onSuccess.value = true
//                    return
//                }

                // Parse the response
                val responseMessage = gson.fromJson(response.body?.string(), ResponseMessageA::class.java)
                println("Signin response: ${responseMessage.message ?: responseMessage.error}")
                name.value = responseMessage.name.toString()
                mobile.value = responseMessage.mobile.toString()
                onSuccess.value = true

                println("onsucess value -> ${onSuccess.value}")
              //return
            }
        }
    })
}