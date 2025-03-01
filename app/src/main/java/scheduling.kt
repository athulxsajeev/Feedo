import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.feedo.BACK
import com.example.feedo.ScheduleRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScheduledFeedingScreen(navController: NavHostController, context: Context, pondName: String) {
    Log.d("ScheduledFeedingScreen", "Pond Name: $pondName") // Log the pondName
    val schedules = remember { mutableStateListOf<Schedule>() } // Use mutableStateListOf
    var showDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }

    // Fetch schedules when the screen loads
    LaunchedEffect(pondName) {
        withContext(Dispatchers.IO) {
            fetchSchedules(pondName) { fetchedSchedules ->
                schedules.clear()
                schedules.addAll(fetchedSchedules)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .padding(top = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Feeding Schedules",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Button(onClick = {
                selectedSchedule = null
                showDialog = true
            }) {
                Text("+", color = Color.White, fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Schedule Cards
        schedules.sortBy { it.time }
        schedules.forEach { schedule ->
            ScheduleCard(
                schedule = schedule,
                onEdit = {
                    selectedSchedule = schedule
                    showDialog = true
                },
                onDelete = {
                    deleteSchedule(schedule.time) {
                        // delete this entry from the list
                        schedules.removeAll {
                            it.time == schedule.time
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Add Schedule Button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {

        }
    }

    if (showDialog) {
        AddScheduleDialog(context, selectedSchedule, pondName) { schedule ->
            schedules.removeAll { it.id == schedule.id }
            schedules.add(schedule)
            showDialog = false
        }
    }
}

@Composable
fun ScheduleCard(schedule: Schedule, onEdit: () -> Unit, onDelete: () -> Unit) {
    var isEnabled by remember { mutableStateOf(schedule.isEnabled) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = schedule.time,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "Weight: ${schedule.weight}kg",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Valve Duration: ${secondToTime(schedule.weight * 30)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Row {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { newValue ->
                        isEnabled = newValue
                        val updatedSchedule = schedule.copy(isEnabled = newValue)
                        updateSchedule(updatedSchedule) // Send updated value to backend
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Green,
                        uncheckedThumbColor = Color.Red
                    )
                )
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

fun updateScheduleStatus(scheduleId: String, isEnabled: Boolean) {
    val client = OkHttpClient()
    val gson = Gson()

    val requestBody = gson.toJson(mapOf("id" to scheduleId, "is_enabled" to isEnabled))
        .toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("${BACK}update_schedule_status")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                println("Schedule status updated successfully")
            }
        }
    })
}


@Composable
fun AddScheduleDialog(context: Context, schedule: Schedule?, pondName: String, onScheduleAdded: (Schedule) -> Unit) {
    val calendar = Calendar.getInstance()
    var selectedTime by remember {
        mutableStateOf(
            schedule?.time ?: SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(calendar.time)
        )
    }
    var weight by remember { mutableStateOf(schedule?.weight?.toString() ?: "") }
    var autoTime by remember { mutableStateOf("") }
    var isManualTime by remember { mutableStateOf(schedule != null) }


    fun calculateAutoTime(weight: Int): String {
        val newTime = Calendar.getInstance().apply { add(Calendar.MINUTE, weight) }
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(newTime.time)
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("${if (schedule == null) "Set" else "Edit"} Feeding Schedule") },
        text = {
            Column {
                Button(onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            selectedTime = String.format("%02d:%02d", hour, minute)
                            isManualTime = true
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                }) {
                    Text("Select Time")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = weight,
                    onValueChange = {
                        weight = it
                    },
                    label = { Text("Enter Weight (kg)") }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Auto Time: $autoTime", fontSize = 14.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalTime = if (isManualTime) selectedTime else autoTime
                val newSchedule =
                    schedule?.copy(time = finalTime, weight = weight.toIntOrNull() ?: 0,isEnabled = true)
                        ?: Schedule(
                            UUID.randomUUID().toString(),
                            finalTime,
                            weight.toIntOrNull() ?: 0,
                            true
                        )
                onScheduleAdded(newSchedule)
                sendToBackend(newSchedule, pondName)
            }) {
                Text("Save")
            }
        }
    )
}

fun secondToTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

fun sendToBackend(schedule: Schedule, pondName: String) {
    val client = OkHttpClient()
    val gson = Gson()

    val requestBody = gson.toJson(
        mapOf(
            "time" to schedule.time,
            "weight" to schedule.weight,
            "isEnabled" to schedule.isEnabled,
            "user_email" to "athul@gmail.com",
            "pond_name" to pondName  // Include pond_name in the request
        )
    ).toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("$BACK/save_schedule")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Failed to send schedule: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (response.isSuccessful) {
                    println("Schedule saved successfully")
                } else {
                    println("Failed to save schedule: ${response.message}")
                }
            }
        }
    })
}


fun fetchSchedules(pondName: String, callback: (List<Schedule>) -> Unit) {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://f43jd2nv-5000.asse.devtunnels.ms/get_schedules/$pondName")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("fetchSchedules", "Failed to fetch schedules: ${e.message}") // Log the error
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { json ->
                Log.d("fetchSchedules", "Fetched schedules: $json") // Log the fetched schedules
                val fetchedSchedules = Gson().fromJson(json, ScheduleResponse::class.java).schedules
                callback(fetchedSchedules)
            }
        }
    })
}

fun deleteSchedule(scheduleId: String, onSuccess: () -> Unit) {
    val client = OkHttpClient()
    val gson = Gson()

    val requestBody = gson.toJson(mapOf("time" to scheduleId))
        .toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("${BACK}delete_schedule")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                onSuccess()
            }
        }
    })
}

fun updateSchedule(schedule: Schedule) {
    val client = OkHttpClient()
    val gson = Gson()

    val requestBody = gson.toJson(
        mapOf(
            "id" to schedule.id,  // Ensure ID is sent to find and update the correct document
            "time" to schedule.time,
            "weight" to schedule.weight,
            "isEnabled" to schedule.isEnabled,
            "user_email" to "athul@gmail.com"
        )
    ).toRequestBody("application/json".toMediaTypeOrNull())

    println("Updating Schedule: $requestBody") // Debugging

    val request = Request.Builder()
        .url("$BACK/update_schedule") // Use an update endpoint instead of save_schedule
        .put(requestBody) // Use PUT for updates
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Failed to update schedule: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                println("Schedule updated successfully: ${response.message}")
            }
        }
    })
}


data class ScheduleResponse(val schedules: List<Schedule>)
data class Schedule(
    val id: String,
    val time: String,
    val weight: Int,
    val isEnabled: Boolean,
    val duration: Int = 0 // Default duration
)