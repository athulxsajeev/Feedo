// Schedule.kt
package com.example.feedo
import com.google.gson.annotations.SerializedName

data class Schedule(
    val id: String,
    val time: String,
    val weight: Int,
    val isEnabled: Boolean
)



data class ScheduleRequest(
    @SerializedName("time") val time: String,
    @SerializedName("weight") val weight: Int,
    @SerializedName("user_email") val userEmail: String
)
