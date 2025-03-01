package com.example.feedo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull

@Composable
fun FoodLevelScreen(navController: NavHostController, pondId: String) {
    var foodLevel by remember { mutableStateOf(50) } // Start with a full food level (50kg)
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(pondId) {
        withContext(Dispatchers.IO) {
            // Fetch the total weight fed to the pond from the feeding history
            fetchTotalWeightFed(pondId) { totalWeightFed ->
                foodLevel = (50 - totalWeightFed).coerceAtLeast(0) // Ensure food level doesn't go below 0
                loading = false
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Food Level") })
    }) { paddingValues ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Available Food: $foodLevel kg", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                ) {
                    val barColor = when {
                        foodLevel <= 10 -> Color.Red
                        foodLevel <= 25 -> Color.Yellow
                        else -> Color.Green
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(foodLevel / 50f) // Scale to percentage of 50kg
                            .background(barColor, shape = RoundedCornerShape(8.dp))
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    updateFoodLevel(pondId, foodLevel) { success ->
                        if (success) {
                            // Optionally, navigate back or show a confirmation message
                            navController.popBackStack()
                        } else {
                            // Handle the error
                        }
                    }
                }) {
                    Text("Update Food Level")
                }
            }
        }
    }
}

fun fetchTotalWeightFed(pondId: String, callback: (Int) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://f43jd2nv-5000.asse.devtunnels.ms/feeding-history") // Replace with your actual backend URL
        .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(0)
        }
        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                val feedingHistory = Gson().fromJson(body, Array<CompletedSchedule>::class.java).toList()
                val totalWeight = feedingHistory.filter { it.pond_name == pondId }.sumOf { it.weight }
                callback(totalWeight)
            } ?: callback(0)
        }
    })
}

fun updateFoodLevel(pondId: String, foodLevel: Int, callback: (Boolean) -> Unit) {
    val client = OkHttpClient()
    val mediaType = "application/json".toMediaTypeOrNull()
    val body = RequestBody.create(mediaType, "{ \"pond_name\": \"$pondId\", \"food_level\": $foodLevel }")
    val request = Request.Builder()
        .url("https://f43jd2nv-5000.asse.devtunnels.ms/update_food_level") // Replace with your actual backend URL
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(false)
        }

        override fun onResponse(call: Call, response: Response) {
            callback(response.isSuccessful)
        }
    })
}
