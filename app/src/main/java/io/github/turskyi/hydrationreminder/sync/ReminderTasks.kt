package io.github.turskyi.hydrationreminder.sync

import android.content.Context
import io.github.turskyi.hydrationreminder.utilities.NotificationUtils
import io.github.turskyi.hydrationreminder.utilities.PreferenceUtilities

object ReminderTasks {
    const val ACTION_INCREMENT_WATER_COUNT = "increment-water-count"
    const val ACTION_DISMISS_NOTIFICATION = "dismiss-notification"
    const val ACTION_CHARGING_REMINDER = "charging-reminder"

    fun executeTask(context: Context, action: String) {
        when (action) {
            ACTION_INCREMENT_WATER_COUNT -> incrementWaterCount(context)
            ACTION_DISMISS_NOTIFICATION -> NotificationUtils.clearAllNotifications(context)
            ACTION_CHARGING_REMINDER -> issueChargingReminder(context)
        }
    }

    private fun incrementWaterCount(context: Context) {
        PreferenceUtilities.incrementWaterCount(context)
        NotificationUtils.clearAllNotifications(context)
    }

    private fun issueChargingReminder(context: Context) {
        PreferenceUtilities.incrementChargingReminderCount(context)
        NotificationUtils.remindUserBecauseCharging(context)
    }
}