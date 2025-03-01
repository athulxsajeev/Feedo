import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavHostController
import com.example.feedo.R
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PHLevelScreen(navController: NavHostController, pondId: String?) {
    var phLevel by remember { mutableStateOf(7.0) }
    var status by remember { mutableStateOf("Your PH range is optimal") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Optional: display which pond's PH is being shown.
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        if (pondId != null) {
            Text(
                text = "PH Level for Pond: $pondId",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(15000) // 15 seconds delay
            withContext(Dispatchers.IO) {
                phLevel = Random.nextDouble(4.0, 12.0)
                status = when {
                    phLevel < 6.5 -> {
                        sendPHNotification(context, "PH Level Alert", "Your water PH level is LOW! ⚠️")
                        "Your water PH level is LOW! ⚠️"
                    }
                    phLevel > 9.0 -> {
                        sendPHNotification(context, "PH Level Alert", "Your water PH level is HIGH! ⚠️")
                        "Your water PH level is HIGH! ⚠️"
                    }
                    else -> "Your PH range is optimal ✅"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your water PH level is: ${"%.2f".format(phLevel)}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = status,
            fontSize = 18.sp,
            color = if (phLevel < 6.5 || phLevel > 9.0) Color.Red else Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Back to Home")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun sendPHNotification(context: Context, title: String, message: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "ph_level_channel"

    val channel = NotificationChannel(
        channelId,
        "PH Level Notifications",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        enableVibration(true)
        enableLights(true)
    }
    notificationManager.createNotificationChannel(channel)

    val notificationId = System.currentTimeMillis().toInt()

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_ph)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(notificationId, notification)
}

