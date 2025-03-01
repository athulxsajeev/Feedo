package com.example.feedo

import FeedingHistoryScreen
import ManualFeedingScreen
import PHLevelScreen
import ScheduledFeedingScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.feedo.ui.theme.FeedoTheme
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.io.IOException

class ManualFeedingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FeedoTheme {
                ManualFeedingScreen()
            }
        }
    }
}

@Composable
fun ManualFeedingScreen(navController: NavHostController? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Feeding Control", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { /* Trigger feeding mechanism */ }) {
            Text("Start/stop")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { navController?.navigate("main_interface") }) {
            Text("Back to Home")
        }
    }
}

data class User(val email: String, val password: String)

private val usersList = mutableListOf<User>()

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FeedoTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { FeedoScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("sign_up") { SignUpScreen(navController) }
                    composable("sign_up_success") { SignUpSuccessScreen(navController) }
                    // Change manual feeding route to show pond list for manual feeding
                    composable("pond_list_for_manual_feeding") { PondListForManualFeedingScreen(navController) }
                    // New route: Manual feeding screen accepts a pond parameter.
                    composable("manual_feeding/{pondId}") { backStackEntry ->
                        val pondId = backStackEntry.arguments?.getString("pondId") ?: ""
                        ManualFeedingScreen(navController, pondId)
                    }
                    composable("scheduled_feeding") { PondListForSchedulesScreen(navController) }
                    composable("feeding_history") { FeedingHistoryScreen() }
                    // Replace the previous "ph_level" route with a two-step flow:
                    composable("pond_list_for_ph") {
                        // New Pond List for PH Level selection
                        PondListForPHScreen(navController)
                    }
                    composable("ph_level/{pondId}") { backStackEntry ->
                        val pondId = backStackEntry.arguments?.getString("pondId")
                        PHLevelScreen(navController, pondId)
                    }
                    composable("main_interface") { MainInterfaceScreen(navController) }
                    composable("add_pond") { AddPondScreen(navController) }
                    composable("scheduling/{pondName}") { backStackEntry ->
                        // Extract the actual pond name
                        val pondName = backStackEntry.arguments?.getString("pondName") ?: ""
                        ScheduledFeedingScreen(
                            navController,
                            context = this@MainActivity,
                            pondName = pondName
                        )
                    }
                    composable("pond_list_for_completed_schedules") {
                        PondListForCompletedSchedulesScreen(navController)
                    }
                    composable("completed_schedules/{pondId}") { backStackEntry ->
                        val pondId = backStackEntry.arguments?.getString("pondId") ?: ""
                        CompletedSchedulesScreen(navController, pondId)
                    }
                    composable("pond_list_for_food_level") {
                        PondListForFoodLevelScreen(navController)
                    }
                    composable("food_level/{pondId}") { backStackEntry ->
                        val pondId = backStackEntry.arguments?.getString("pondId") ?: ""
                        FoodLevelScreen(navController, pondId)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedoScreen(navController: NavHostController) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val dynamicPadding = when {
            maxWidth < 360.dp -> 8.dp
            maxWidth < 600.dp -> 16.dp
            else -> 24.dp
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dynamicPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FEEDO",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("login") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "Login with Mail", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("sign_up") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text(text = "Sign Up", color = Color.Black, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val loginError = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = loginError.value, color = Color.Red, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        val onSuccess = remember { mutableStateOf(false) }
        Button(
            onClick = {
                if (email.value.isBlank() || password.value.isBlank()) {
                    loginError.value = "All fields must be filled"
                } else if (!isValidEmail(email.value)) {
                    loginError.value = "Invalid email format"
                } else if (!isValidEmail(email.value)) {
                    loginError.value = "Invalid email format"
                } else {
                    signinUserBackend(email.value, password.value, loginError, onSuccess)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(text = "Login", color = Color.White, fontSize = 16.sp)
        }
        LaunchedEffect(onSuccess.value) {
            if (onSuccess.value) {
                navController.navigate("main_interface")
            } else if (loginError.value.isNotEmpty()) {
                loginError.value = "Invalid email or password"
            }
        }
    }
}

@Composable
fun SignUpScreen(navController: NavHostController) {
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val mobile = remember { mutableStateOf("") }
    val signUpError = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign Up",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = mobile.value,
            onValueChange = { mobile.value = it },
            label = { Text("Mobile no") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = signUpError.value, color = Color.Red, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.value.isBlank() || email.value.isBlank() || password.value.isBlank() || mobile.value.isBlank()) {
                    signUpError.value = "All fields must be filled"
                } else if (!isValidEmail(email.value)) {
                    signUpError.value = "Invalid email format"
                } else if (!isValidPassword(password.value)) {
                    signUpError.value = "Password must be at least 6 characters"
                } else {
                    signupUser(name.value, password.value, email.value, mobile.value)
                    navController.navigate("sign_up_success")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(text = "Sign Up", color = Color.White, fontSize = 16.sp)
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}

fun signinUser(
    email: String,
    password: String,
    loginError: MutableState<String>,
    onSuccess: MutableState<Boolean>
) {
    val client = OkHttpClient()
    val gson = Gson()

    val requestBody = gson.toJson(mapOf("email" to email, "password" to password))
        .toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://f43jd2nv-5000.asse.devtunnels.ms/signin")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            loginError.value =
                "Unable to resolve host. Please check your internet connection and try again."
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val user = gson.fromJson(responseBody, User::class.java)
                onSuccess.value = true
            } else {
                loginError.value = "Invalid email or password"
            }
        }
    })
}

@Composable
fun SignUpSuccessScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You are successfully signed up!\nNow press back and login.",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("home") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(text = "Back to Home", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun MainInterfaceScreen(navController: NavHostController) {
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
                TopSection(userName = name.value, phoneNumber = mobile.value)

                Spacer(modifier = Modifier.height(40.dp))

                MainFeaturesSection(navController)

                Spacer(modifier = Modifier.height(60.dp))

                Spacer(modifier = Modifier.height(90.dp))

                
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

@Composable
fun TopSection(userName: String, phoneNumber: String) {
    val showDialog = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                androidx.compose.material.Text(
                    userName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                androidx.compose.material.Text(phoneNumber, fontSize = 16.sp, color = Color.Gray)
            }
            androidx.compose.material.Button(
                onClick = { showDialog.value = true },
                colors = androidx.compose.material.ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                shape = RoundedCornerShape(5.dp)
            ) {
                androidx.compose.material.Text("Report a complaint!", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        androidx.compose.material.Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFFEAF6FF),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    androidx.compose.material.Text(
                        "Your Systems:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    androidx.compose.material.Text(
                        "Model No:13323",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    androidx.compose.material.Text(
                        "System Count:1",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    androidx.compose.material.TextButton(onClick = { /* Know more */ }) {
                        androidx.compose.material.Text("Know More", color = Color.Blue)
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "Report a Complaint") },
            text = { Text(text = "Please contact us at: +91 8547955920") },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }
}
@Composable
fun MainFeaturesSection(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureButton("Scheduled Feeding", painterResource(id = R.drawable.ic_clock)) {
                navController.navigate("scheduled_feeding")
            }
            FeatureButton("Manual Feeding", painterResource(id = R.drawable.ic_manual)) {
                navController.navigate("pond_list_for_manual_feeding")
            }
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Change Feeding History button to navigate to the new pond list for completed schedules.
            FeatureButton("Feeding History", painterResource(id = R.drawable.ic_history)) {
                navController.navigate("pond_list_for_completed_schedules")
            }
            FeatureButton("Water PH Level", painterResource(id = R.drawable.ic_ph)) {
                // Navigate first to pond list for PH selection
                navController.navigate("pond_list_for_ph")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureButton("Food Level", painterResource(id = R.drawable.ic_ph)) {
                navController.navigate("pond_list_for_food_level")
            }
        }
    }
}

@Composable
fun FeatureButton(name: String, icon: Painter, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(25.dp)) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
        ) {
            androidx.compose.material.Icon(
                painter = icon,
                contentDescription = name,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        androidx.compose.material.Text(name, fontSize = 18.sp, color = Color.Black)
    }
}


