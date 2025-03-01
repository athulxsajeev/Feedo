package com.example.feedo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

// Data model for a completed schedule (as saved in past_schedules)
data class CompletedSchedule(
    val time: String = "",
    val weight: Int = 0,
    val user_email: String = "",
    val pond_name: String = "",
    val date: String = ""
)

@Composable
fun CompletedSchedulesScreen(navController: NavHostController, pondName: String) {
    var schedules by remember { mutableStateOf<List<CompletedSchedule>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(pondName) {
        withContext(Dispatchers.IO) {
            fetchCompletedSchedules { allSchedules ->
                // Filter solely by pond name (remove time filtering)
                schedules = allSchedules.filter { it.pond_name.equals(pondName, ignoreCase = true) }
                loading = false
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text(text = "Completed Schedules", modifier = Modifier.padding(bottom = 16.dp))
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (schedules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No completed schedules available for $pondName.")
            }
        } else {
            LazyColumn {
                items(schedules) { schedule ->
                    ScheduleCard(schedule)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(schedule: CompletedSchedule) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Time: ${schedule.time}")
            Text(text = "Weight: ${schedule.weight} kg")
            Text(text = "User: ${schedule.user_email}")
            Text(text = "Date: ${schedule.date}")
        }
    }
}

fun fetchCompletedSchedules(callback: (List<CompletedSchedule>) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://f43jd2nv-5000.asse.devtunnels.ms/feeding-history")
        .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(emptyList())
        }
        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                val schedules = Gson().fromJson(body, Array<CompletedSchedule>::class.java).toList()
                callback(schedules)
            } ?: callback(emptyList())
        }
    })
}
