package io.github.turskyi.hydrationreminder.sync

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService

/**
 * An [JobIntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
class WaterReminderIntentService : JobIntentService() {
    companion object {
        const val JOB_ID = 2
    }
    override fun onHandleWork(intent: Intent) {
        val action = intent.action
        if (action != null) {
            ReminderTasks.executeTask(this, action)
        }
    }

    fun enqueueWork(context: Context, intent: Intent) {
        enqueueWork(context, WaterReminderIntentService::class.java, JOB_ID, intent)
    }
}