package com.example.feedo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Remove the duplicate data class declaration if it is defined elsewhere.
// data class PondData(
//     val _id: String = "",
//     val pond_name: String = "",
//     val feeder_id: String = "",
//     val breed_type: String = ""
// )

@Composable
fun PondListForCompletedSchedulesScreen(navController: NavHostController) {
    var ponds by remember { mutableStateOf<List<PondData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            fetchPondsForCompletedSchedules { fetchedPonds ->
                ponds = fetchedPonds
                loading = false
            }
        }
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Select a Pond") })
    }) { paddingValues ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(ponds) { pond ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("completed_schedules/${pond._id}")
                            },
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Pond: ${pond.pond_name}", style = MaterialTheme.typography.h6)
                            Text(text = "Feeder ID: ${pond.feeder_id}")
                            Text(text = "Breed: ${pond.breed_type}")
                        }
                    }
                }
            }
        }
    }
}

fun fetchPondsForCompletedSchedules(callback: (List<PondData>) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://f43jd2nv-5000.asse.devtunnels.ms/get_ponds")
        .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(emptyList())
        }
        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                val fetchedPonds = Gson().fromJson(body, Array<PondData>::class.java).toList()
                callback(fetchedPonds)
            } ?: callback(emptyList())
        }
    })
}
