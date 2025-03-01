import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedingHistoryScreen() {
    // Store fetched data persistently
    var schedules by rememberSaveable { mutableStateOf<List<Scheduledata>>(emptyList()) }


    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            fetchFeedingHistory { fetchedSchedules ->
                schedules = fetchedSchedules
            }
        }
    }
    // Function to fetch feeding history


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Feeding History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Text(
            text = "Date: $currentDate",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))


        // Display Previously Fetched Data
        if (schedules.isEmpty()) {
            Text(
                text = "No feeding history available.",
                fontSize = 16.sp,
                color = Color.Gray
            )
        } else {
            LazyColumn {
                items(schedules) { scheduledata ->
                    FeedingHistoryCard(schedule = scheduledata)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun FeedingHistoryCard(schedule: Scheduledata) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Schedule Date: ${schedule.date}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Time: ${schedule.time}",
                fontSize = 14.sp,
                color = Color.Black
            )
            Text(
                text = "Quantity: ${schedule.weight} kg",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "User Email: ${schedule.user_email}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            // Button inside the card
            Spacer(modifier = Modifier.height(16.dp)) // Adding some space before the button
            Button(
                onClick = {
                    Log.d("FeedingHistory", "Button pressed for schedule: ${schedule.time}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
            ) {
                Text(text = "Action", color = Color.White)
            }
        }
    }
}


fun fetchFeedingHistory(callback: (List<Scheduledata>) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://f43jd2nv-5000.asse.devtunnels.ms/feeding-history") // Your Flask Server URL
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                response.body?.string()?.let { body ->
                   val fetchedSchedules = Gson().fromJson(body, Array<Scheduledata>::class.java).toList()
                    callback(fetchedSchedules)
                }
            } else {
                Log.e("FeedingHistory", "Error fetching feeding history")
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            Log.e("FeedingHistory", "Request failed: ${e.message}")
        }
    })
}

// Schedule Data Model (Updated)
data class Scheduledata(
    val time: String = "",
    val weight: Int = 0,
    val user_email: String = "",
    val date: String = ""
)