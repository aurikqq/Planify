package com.aurikqq.planify

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val CHANNEL_ID = "planify_channel_id"

fun createNotificationChannel(context: Context) {
    val CHANNEL_NAME = context.getString(R.string.notifications_channel_name)
    val CHANNEL_DESCRIPTION = context.getString(R.string.notifications_channel_description)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@SuppressLint("MissingPermission")
fun showPlansNotification(
    context: Context,
    title: String,
    text: String,
    notificationId: Int = 1
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
        notify(notificationId, builder.build())
    }
}

@SuppressLint("MissingPermission")
fun showPlansResetNotification(
    context: Context,
    title: String,
    text: String,
    notificationId: Int = 2
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
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setStyle(NotificationCompat.BigTextStyle().bigText(""))

    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())
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

fun schedulePlanReminders(context: Context) {
    val planReminderRequest =
        PeriodicWorkRequestBuilder<NotificationsWorker>(2, TimeUnit.HOURS)
            .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "planReminderWork",
        ExistingPeriodicWorkPolicy.KEEP,
        planReminderRequest
    )
}

fun sendTestNotification(context: Context) {
    val testReminderRequest = OneTimeWorkRequestBuilder<NotificationsWorker>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "testPlanReminder",
        ExistingWorkPolicy.KEEP,
        testReminderRequest
    )

    val testPlansResetNotificationRequest = OneTimeWorkRequestBuilder<PlansResetNotification>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "testDailyPlanResetNotification",
        ExistingWorkPolicy.KEEP,
        testPlansResetNotificationRequest
    )
}

fun cancelPlanReminders(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork("planReminderWork")
}

class NotificationsWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    val notificationTitles = listOf(
        context.getString(R.string.notification_title_01),
        context.getString(R.string.notification_title_02),
        context.getString(R.string.notification_title_03))
    override suspend fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val plans = sharedPreferences.getString(KEY_PLANS, null)
        if (!plans.isNullOrEmpty()) {
            showPlansNotification(applicationContext, notificationTitles.random(),
                context.getString(R.string.notification_text) + "\n" + plans)
        }
        return Result.success()
    }
}

class PlansReset(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        return try {
            cancelPlanReminders(context = applicationContext)
            val sharedPreferences = applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit {
                remove(KEY_PLANS)
                putBoolean(KEY_HAVE_PLANS, false)
            }
            Result.success()
        }
        catch (e: Exception) {
            Result.failure()
        }
    }
}

fun scheduleReset(context: Context) {
    val workManager = WorkManager.getInstance(context)
    val calendar = Calendar.getInstance().apply() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val initialDelay = calendar.timeInMillis - System.currentTimeMillis()
    val resetRequest = PeriodicWorkRequestBuilder<PlansReset>(
        1,
        TimeUnit.DAYS
    )
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork(
        "daily_plan_reset",
        ExistingPeriodicWorkPolicy.KEEP,
        resetRequest
    )
}

class PlansResetNotification(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    val resetNotificationTexts = listOf(
        context.getString(R.string.reset_notification_text_01),
        context.getString(R.string.reset_notification_text_01),
        context.getString(R.string.reset_notification_text_01)
    )
    override suspend fun doWork(): Result {
        return try {
            showPlansResetNotification(
                applicationContext,
                context.getString(R.string.reset_notification_title),
                resetNotificationTexts.random()
            )
            val sharedPreferences = applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit {
                remove(KEY_PLANS)
                putBoolean(KEY_HAVE_PLANS, false)
            }
            Result.success()
        }
        catch (e: Exception) {
            Result.failure()
        }
    }
}

fun scheduleResetNotification(context: Context) {
    val workManager = WorkManager.getInstance(context)
    val calendar = Calendar.getInstance().apply() {
        set(Calendar.HOUR_OF_DAY, 7)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val initialDelay = calendar.timeInMillis - System.currentTimeMillis()
    val resetRequest = PeriodicWorkRequestBuilder<PlansResetNotification>(
        1,
        TimeUnit.DAYS
    )
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork(
        "daily_plan_reset_notification",
        ExistingPeriodicWorkPolicy.KEEP,
        resetRequest
    )
}