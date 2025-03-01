package com.example.feedo

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun AddPondScreen(navController: NavHostController) {
    var pondName by remember { mutableStateOf("") }
    var feederId by remember { mutableStateOf("") }
    var breedType by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Pond") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main_interface") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = pondName,
                onValueChange = { pondName = it },
                label = { Text("Pond Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = feederId,
                onValueChange = { feederId = it },
                label = { Text("Feeder ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = breedType,
                onValueChange = { breedType = it },
                label = { Text("Breed Type") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    addPond(pondName, feederId, breedType) { result ->
                        message = result
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Pond")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}

fun addPond(pondName: String, feederId: String, breedType: String, callback: (String) -> Unit) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("https://f43jd2nv-5000.asse.devtunnels.ms/add_pond") // Replace with your backend address
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.doOutput = true
            httpURLConnection.setRequestProperty("Content-Type", "application/json")
            val jsonParam = JSONObject().apply {
                put("pond_name", pondName)
                put("feeder_id", feederId)
                put("breed_type", breedType)
            }
            val outputBytes = jsonParam.toString().toByteArray(Charsets.UTF_8)
            httpURLConnection.outputStream.write(outputBytes)
            httpURLConnection.outputStream.flush()
            httpURLConnection.outputStream.close()

            val responseCode = httpURLConnection.responseCode
            if (responseCode == 201) {
                callback("Pond added successfully!")
            } else {
                callback("Failed to add pond: $responseCode")
            }
        } catch (e: Exception) {
            callback("Error: ${e.message}")
        }
    }
}