import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID
import androidx.navigation.NavHostController
import com.example.feedo.Schedule


@SuppressLint("DefaultLocale")
@Composable
fun AddScheduleScreen(navController: NavHostController, onSave: (Schedule) -> Unit) {
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }
    var isAm by remember { mutableStateOf(true) }
    var weight by remember { mutableStateOf("") } // Define weight variable

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Schedule",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Time Picker Section
        Text(
            text = "Set Time:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hour Slider
        Text(text = "Hour: $hour", fontSize = 16.sp, color = Color.Black)
        Slider(
            value = hour.toFloat(),
            onValueChange = { hour = it.toInt() },
            valueRange = 1f..12f,
            steps = 11,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color(0xFFFFA500),
                inactiveTrackColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Minute Slider
        Text(text = "Minute: $minute", fontSize = 16.sp, color = Color.Black)
        Slider(
            value = minute.toFloat(),
            onValueChange = { minute = it.toInt() },
            valueRange = 0f..59f,
            steps = 59,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color(0xFFFFA500),
                inactiveTrackColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AM/PM Switch
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AM", fontSize = 16.sp, color = Color.Black)
            Switch(
                checked = isAm,
                onCheckedChange = { isAm = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFFA500),
                    uncheckedThumbColor = Color(0xFFFFA500)
                )
            )
            Text("PM", fontSize = 16.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight Input
        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Enter Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save and Cancel Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.navigateUp() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Cancel", color = Color.White)
            }
            Button(
                onClick = {
                    val time = String.format("%02d:%02d %s", hour, minute, if (isAm) "AM" else "PM")
                    val newSchedule = Schedule(UUID.randomUUID().toString(), time, weight.toIntOrNull() ?: 0, true)
                    onSave(newSchedule)
                    scheduleFeedingNotification(navController.context, newSchedule)
                    navController.navigateUp()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA500))
            ) {
                Text("Save", color = Color.White)
            }
        }
    }
}