import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*
import com.example.feedo.FeedingNotificationReceiver // Add this import
import com.example.feedo.Schedule


fun scheduleFeedingNotification(context: Context, schedule: Schedule) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val feedingIntent = Intent(context, FeedingNotificationReceiver::class.java).apply {
        putExtra("title", "Feeding Started")
        putExtra("message", "The valve has opened and feeding has started.")
    }
    val feedingPendingIntent = PendingIntent.getBroadcast(context, 0, feedingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val feedingEndIntent = Intent(context, FeedingNotificationReceiver::class.java).apply {
        putExtra("title", "Feeding Ended")
        putExtra("message", "The valve has closed and feeding has ended.")
    }
    val feedingEndPendingIntent = PendingIntent.getBroadcast(context, 1, feedingEndIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val feedingTime = Calendar.getInstance().apply {
        time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(schedule.time)!!
    }

    alarmManager.setExact(AlarmManager.RTC_WAKEUP, feedingTime.timeInMillis, feedingPendingIntent)

    val feedingEndTime = feedingTime.apply {
        add(Calendar.SECOND, schedule.weight * 30) // Assuming weight * 30 seconds is the duration
    }

    alarmManager.setExact(AlarmManager.RTC_WAKEUP, feedingEndTime.timeInMillis, feedingEndPendingIntent)
}