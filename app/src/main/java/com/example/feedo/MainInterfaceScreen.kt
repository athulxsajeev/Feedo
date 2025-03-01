import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // Import material3 components
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // Correct import for Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.feedo.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import androidx.compose.ui.text.style.TextAlign


// The rest of your code remains unchanged



@Composable
fun MainInterfaceScreen(navController: NavHostController) {
    // Correct way to instantiate the ViewModel
    val viewModel: UserViewModel = viewModel()

    val user by viewModel.user.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints {
            val dynamicPadding = when {
                maxWidth < 360.dp -> 8.dp
                maxWidth < 600.dp -> 16.dp
                else -> 24.dp
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(dynamicPadding)
            ) {
                TopSection(userName = user.name, phoneNumber = user.phoneNumber)

                Spacer(modifier = Modifier.height(40.dp))

                MainFeaturesSection(navController = navController)  // Pass navController here

                Spacer(modifier = Modifier.height(60.dp))

                FoodLevelButton(navController) // ADD FoodLevelButton here

                Spacer(modifier = Modifier.height(90.dp))

                NavigationBar(navController = navController)
            }
        }
        FloatingActionButton(
            onClick = { navController.navigate("add_pond") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Pond"
            )
        }
    }
}


data class User(
    val name: String = "User",
    val phoneNumber: String = "N/A"
)

class UserViewModel : ViewModel() {
    private val _user = MutableStateFlow(User()) // Default empty user
    val user: StateFlow<User> = _user

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            try {
                val document = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document("user_id") // Replace with dynamic user ID
                    .get()
                    .await() // Use Kotlin Coroutine

                if (document.exists()) {
                    val name = document.getString("name") ?: "User"
                    val phone = document.getString("phoneNumber") ?: "N/A"
                    _user.value = User(name, phone)
                }
            } catch (e: Exception) {
                println("Error fetching user: ${e.message}")
            }
        }
    }
}

@Composable
fun TopSection(userName: String, phoneNumber: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(phoneNumber, fontSize = 16.sp, color = Color.Gray)
            }
            Button(
                onClick = { /* Report a complaint */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Report a complaint!", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // System Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF6FF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your Systems:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Model No:13323", fontSize = 16.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("System Count:1", fontSize = 16.sp, color = Color.Black)
                    TextButton(onClick = { /* Know more */ }) {
                        Text("Know More", color = Color.Blue)
                    }
                }
            }
        }
    }
}


@Composable
fun MainFeaturesSection(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { /* Navigate to Scheduled Feeding */ },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
        ) {
            Text("Scheduled Feeding", color = Color.White)
        }

        Button(
            onClick = { navController.navigate("manual_feeding") },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
        ) {
            Text("Manual Feeding", color = Color.White)
        }

        Button(
            onClick = {

                navController.navigate("FeedingHistoryScreen") },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
        ) {
            Text("Feeding History", color = Color.White)
        }

        Button(
            onClick = { /* Navigate to Water PH Level */ },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
        ) {
            Text("Water PH Level", color = Color.White)
        }
        Button(
            onClick = { navController.navigate("ph_level") },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
        ) {
            Text("Water PH Level", color = Color.White)
        }

        // ADD Food Level Button here
        Button(
            onClick = { navController.navigate("pond_list_for_food_level") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
        ) {
            Text("Food Level", color = Color.White)
        }
    }
}

private fun sendCommandToServer(url: String) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("API_ERROR", "Failed to connect: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            Log.d("API_RESPONSE", "Response: ${response.body?.string()}")
        }
    })
}

@Composable
fun ManualFeedingScreen(navController: NavHostController? = null, pondId: String) {
    // Now you can use pondId to identify which pond feeding is triggered.
    var isTimerRunning by remember { mutableStateOf(false) }
    var timeElapsed by remember { mutableStateOf(0) }
    val WeightFed = timeElapsed/30
    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Manual Feeding Control for Pond: $pondId",
            style = MaterialTheme.typography.headlineSmall, // Use headlineSmall
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Time Elapsed: $timeElapsed sec",
            style = MaterialTheme.typography.titleMedium, // Use titleMedium
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Total Weight Fed : $WeightFed kg",
            style = MaterialTheme.typography.titleMedium, // Use titleMedium
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Start Button
        Button(
            onClick = {
                if (!isTimerRunning) {
                    isTimerRunning = true
                    timerJob = coroutineScope.launch {
                        sendCommandToServer("https://f43jd2nv-5000.asse.devtunnels.ms//start_feeding") // Call backend
                        while (true) {
                            delay(1000)
                            timeElapsed += 1
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            enabled = !isTimerRunning
        ) {
            Text(text = "Start Feeding")
        }

        Button(
            onClick = {
                isTimerRunning = false
                timerJob?.cancel()
                timeElapsed = 0
                sendCommandToServer("https://f43jd2nv-5000.asse.devtunnels.ms/stop_feeding") // Call backend
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            enabled = isTimerRunning
        ) {
            Text(text = "Stop Feeding")
        }

        Spacer(modifier = Modifier.height(20.dp))



        // Back Button
        Button(onClick = {
            navController?.navigate("main_interface?userName=b&phoneNumber=1234567890") {
                popUpTo("main_interface") { inclusive = true }
            }
        }) {
            Text("Back to Home")
        }
    }
}

@Composable
fun FoodLevelIndicator() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Food Level", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(150.dp) // Adjust based on the food level (e.g., 15kg)
                    .background(Color.Green, shape = RoundedCornerShape(8.dp))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0kg", fontSize = 12.sp, color = Color.Gray)
            Text("15kg", fontSize = 12.sp, color = Color.Gray)
            Text("30kg", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun NavigationBar(navController: NavHostController) {
    BottomAppBar(containerColor = Color.Black) {
        // Home Button (Start)
        IconButton(
            onClick = { navController.navigate("home") },
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                tint = Color.White
            )
        }

        // Empty Space to Center Notification Button
        Spacer(modifier = Modifier.weight(1f))

        // Notifications Button (Center)
        IconButton(
            onClick = { navController.navigate("notifications") },
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_bell),
                contentDescription = "Notifications",
                tint = Color.White
            )
        }

        // Empty Space to Balance Layout
        Spacer(modifier = Modifier.weight(1f))

        // Contact Button (End)
        IconButton(
            onClick = { navController.navigate("contact") },
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_call),
                contentDescription = "Phone",
                tint = Color.White
            )
        }
    }
}

@Composable
fun FoodLevelButton(navController: NavHostController) {
    Button(
        onClick = { navController.navigate("pond_list_for_food_level") },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)) // Use ButtonDefaults from material3
    ) {
        Text(text = "Food Level", color = Color.White, fontSize = 16.sp)
    }
}