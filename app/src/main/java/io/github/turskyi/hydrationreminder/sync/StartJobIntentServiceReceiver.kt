package io.github.turskyi.hydrationreminder.sync

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import io.github.turskyi.hydrationreminder.sync.WaterReminderIntentService.Companion.JOB_ID

/**
 * A receiver that acts as a pass-through for enqueueing work to a [androidx.core.app.JobIntentService].
 * @see [https://stackoverflow.com/a/49294691/10636137]
 */
class StartJobIntentServiceReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_SERVICE_CLASS =
            "io.github.turskyi.hydrationreminder.sync.WaterReminderIntentService"
        const val EXTRA_JOB_ID = "io.github.turskyi.hydrationreminder.extra_job_id"

        /**
         * @param intent an Intent meant for a [androidx.core.app.JobIntentService]
         * @return a new Intent intended for use by this receiver based off the passed intent
         */
        fun getIntent(context: Context, intent: Intent, jobId: Int): Intent {
            val component: ComponentName =
                intent.component ?: throw RuntimeException("Missing intent component")
            val newIntent: Intent = Intent(intent)
                .putExtra(EXTRA_SERVICE_CLASS, component.className)
                .putExtra(EXTRA_JOB_ID, jobId)
            newIntent.setClass(context, StartJobIntentServiceReceiver::class.java)
            return newIntent
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.extras == null) {
                throw Exception("No extras found")
            } else {
                val serviceClassName: String = intent.getStringExtra(EXTRA_SERVICE_CLASS)
                    ?: throw Exception("No service class found in extras")
                val serviceClass: Class<*> = Class.forName(serviceClassName)
                if (!JobIntentService::class.java.isAssignableFrom(serviceClass)) {
                    throw Exception("Service class found is not a JobIntentService: " + serviceClass.name)
                }
                intent.setClass(context, serviceClass)

                // getting job id
                if (!intent.extras!!.containsKey(EXTRA_JOB_ID)) {
                    throw Exception("No job ID found in extras")
                }
                val jobId: Int = intent.getIntExtra(EXTRA_JOB_ID, JOB_ID)

                // starting the service
                JobIntentService.enqueueWork(context, serviceClass, jobId, intent)
            }
        } catch (e: Exception) {
            System.err.println("Error starting service from receiver: " + e.message)
        }
    }
}