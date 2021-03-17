package io.github.turskyi.hydrationreminder.utilities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.turskyi.hydrationreminder.MainActivity
import io.github.turskyi.hydrationreminder.R
import io.github.turskyi.hydrationreminder.sync.ReminderTasks
import io.github.turskyi.hydrationreminder.sync.StartJobIntentServiceReceiver
import io.github.turskyi.hydrationreminder.sync.WaterReminderIntentService

/**
 * Utility class for creating hydration notifications
 */
object NotificationUtils {
    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 1138 is in no way significant.
     */
    private const val WATER_REMINDER_NOTIFICATION_ID = 1138

    /**
     * This pending intent id is used to uniquely reference the pending intent
     */
    private const val WATER_REMINDER_PENDING_INTENT_ID = 3417

    /**
     * This notification channel id is used to link notifications to this channel
     */
    private const val WATER_REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel"
    private const val ACTION_DRINK_PENDING_INTENT_ID = 15
    private const val ACTION_IGNORE_PENDING_INTENT_ID = 14

    fun clearAllNotifications(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    fun remindUserBecauseCharging(context: Context) {
        Log.d("===>", "notification")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                WATER_REMINDER_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.main_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(mChannel)
        }
        val notificationBuilder =
            NotificationCompat.Builder(context, WATER_REMINDER_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_drink_notification)
                .setLargeIcon(context.vectorToBitmap(R.drawable.ic_local_drink_black_24px))
                .setContentTitle(context.getString(R.string.charging_reminder_notification_title))
                .setContentText(context.getString(R.string.charging_reminder_notification_body))
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.charging_reminder_notification_body)
                    )
                )
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .addAction(drinkWaterAction(context))
                .addAction(ignoreReminderAction(context))
                .setAutoCancel(true)
        notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
        notificationManager.notify(WATER_REMINDER_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun ignoreReminderAction(context: Context): NotificationCompat.Action {
        val ignoreReminderIntent = Intent(context, WaterReminderIntentService::class.java)
        ignoreReminderIntent.action = ReminderTasks.ACTION_DISMISS_NOTIFICATION
        val ignoreReminderPendingIntent = PendingIntent.getBroadcast(
            context,
            ACTION_IGNORE_PENDING_INTENT_ID,
            StartJobIntentServiceReceiver.getIntent(context, ignoreReminderIntent,
                WaterReminderIntentService.JOB_ID
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Action(
            R.drawable.ic_cancel_black_24px,
            "No, thanks.",
            ignoreReminderPendingIntent
        )
    }

    private fun drinkWaterAction(context: Context): NotificationCompat.Action {
        val incrementWaterCountIntent = Intent(context, WaterReminderIntentService::class.java)
        incrementWaterCountIntent.action = ReminderTasks.ACTION_INCREMENT_WATER_COUNT
        val incrementWaterPendingIntent = PendingIntent.getBroadcast(
            context,
            ACTION_DRINK_PENDING_INTENT_ID,
            StartJobIntentServiceReceiver.getIntent(context, incrementWaterCountIntent,
                WaterReminderIntentService.JOB_ID
            ),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        return NotificationCompat.Action(
            R.drawable.ic_local_drink_black_24px,
            "I did it!",
            incrementWaterPendingIntent
        )
    }

    private fun contentIntent(context: Context): PendingIntent {
        val startActivityIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            WATER_REMINDER_PENDING_INTENT_ID,
            startActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /** returns nullable Bitmap (very important) */
    private fun Context.vectorToBitmap(vector: Int): Bitmap? {
        val res = resources
        return BitmapFactory.decodeResource(res, vector)
    }
}