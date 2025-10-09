package com.aurikqq.planify

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

const val CHANNEL_ID = "planify_channel_id"

/*TODO fix notifs*/

class TimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val sharedPreferences =
            context?.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val repo = Repository(
            sharedPreferences!!, context)
        val currentDate = LocalDate.now().format(
            DateTimeFormatter.ofPattern(
                "dd_MM_yyyy", Locale.getDefault()
            )
        )

        if (repo.havePlansForDate(currentDate)) {

            val plans = context.getString(R.string.notification_text) + "\n" + repo.getPlansForDate(currentDate)

            val notificationTitles = listOf(
                context.getString(R.string.notification_title_01),
                context.getString(R.string.notification_title_02),
                context.getString(R.string.notification_title_03)
            )

            showPlansNotification(context, notificationTitles.random(), plans)
            AlarmScheduler.scheduleRepeatingAlarm(context)
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val calendar = Calendar.getInstance()

            val time = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 2)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            if (calendar.after(time)) {
                showPlansResetNotification(context!!)
            }

            AlarmScheduler.scheduleAlarm(context!!)
            AlarmScheduler.scheduleRepeatingAlarm(context)
        }
    }
}

class ResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val sharedPreferences = context?.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            sharedPreferences?.edit {
                remove(KEY_PLANS)
                putBoolean(KEY_HAVE_PLANS, false)
            }
            AlarmScheduler.cancelNotifications(context as Context)
        }
        catch (e: Exception) {
            Log.d("PlansReset", "Error: $e")
        }
    }
}

object AlarmScheduler {
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(context: Context) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intent = Intent(context, TimeReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    @SuppressLint("ShortAlarm", "ScheduleExactAlarm")
    fun scheduleRepeatingAlarm(context: Context) {
        val interval = 2 * 60 * 60 * 1000L
        val intent = Intent(context, TimeReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + interval,
            pendingIntent
        )
    }

    fun cancelNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimeReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun schedulePlansReset(context: Context) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intent = Intent(context, ResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}

fun createNotificationChannel(context: Context) {
    val channelName = context.getString(R.string.notifications_channel_name)
    val channelDescription = context.getString(R.string.notifications_channel_description)

    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
        description = channelDescription
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

@SuppressLint("MissingPermission")
fun showPlansNotification(
    context: Context,
    title: String?,
    text: String?,
) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.icon_with_top)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setStyle(NotificationCompat.BigTextStyle().bigText(""))

    with(NotificationManagerCompat.from(context)) {
        notify(1, builder.build())
    }
}

@SuppressLint("MissingPermission")
fun showPlansResetNotification(context: Context) {
    val resetNotificationTexts = listOf(
        context.getString(R.string.reset_notification_text_01),
        context.getString(R.string.reset_notification_text_02),
        context.getString(R.string.reset_notification_text_03)
    )

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.icon_with_top)
        .setContentTitle(context.getString(R.string.reset_notification_title))
        .setContentText(resetNotificationTexts.random())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        //.setStyle(NotificationCompat.BigTextStyle().bigText(""))

    with(NotificationManagerCompat.from(context)) {
        notify(2, builder.build())
    }
}

@Composable
fun RequestNotificationsPermission() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    SideEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

//fun schedulePlanReminders(context: Context) {
//    val planReminderRequest =
//        PeriodicWorkRequestBuilder<NotificationsWorker>(2, TimeUnit.HOURS)
//            .build()
//
//    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//        "planReminderWork",
//        ExistingPeriodicWorkPolicy.KEEP,
//        planReminderRequest
//    )
//}
//
//fun cancelPlanReminders(context: Context) {
//    WorkManager.getInstance(context).cancelUniqueWork("planReminderWork")
//}
//
//class NotificationsWorker(val context: Context, workerParams: WorkerParameters) :
//    CoroutineWorker(context, workerParams) {
//    val notificationTitles = listOf(
//        context.getString(R.string.notification_title_01),
//        context.getString(R.string.notification_title_02),
//        context.getString(R.string.notification_title_03))
//    override suspend fun doWork(): Result {
//        val sharedPreferences = applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
//        val plans = sharedPreferences.getString(KEY_PLANS, null)
//        if (!plans.isNullOrEmpty()) {
//            showPlansNotification(applicationContext, notificationTitles.random(),
//                context.getString(R.string.notification_text) + "\n" + plans)
//        }
//        return Result.success()
//    }
//}
//
//class PlansReset(context: Context, parameters: WorkerParameters) :
//    CoroutineWorker(context, parameters) {
//    override suspend fun doWork(): Result {
//        return try {
//            cancelPlanReminders(context = applicationContext)
//            val sharedPreferences = applicationContext.getSharedPreferences(
//                PREFERENCES_NAME,
//                Context.MODE_PRIVATE
//            )
//            sharedPreferences.edit {
//                remove(KEY_PLANS)
//                putBoolean(KEY_HAVE_PLANS, false)
//            }
//            Result.success()
//        }
//        catch (e: Exception) {
//            Log.d("PlansReset", "Error: $e")
//            Result.failure()
//        }
//    }
//}
//
//fun scheduleReset(context: Context) {
//    val workManager = WorkManager.getInstance(context)
//    val calendar = Calendar.getInstance().apply {
//        set(Calendar.HOUR_OF_DAY, 0)
//        set(Calendar.MINUTE, 0)
//        set(Calendar.SECOND, 0)
//        if (before(Calendar.getInstance())) {
//            add(Calendar.DAY_OF_MONTH, 1)
//        }
//    }
//    val initialDelay = calendar.timeInMillis - System.currentTimeMillis()
//    val resetRequest = PeriodicWorkRequestBuilder<PlansReset>(
//        1,
//        TimeUnit.DAYS
//    )
//        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
//        .build()
//
//    workManager.enqueueUniquePeriodicWork(
//        "daily_plan_reset",
//        ExistingPeriodicWorkPolicy.KEEP,
//        resetRequest
//    )
//}