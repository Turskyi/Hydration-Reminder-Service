package io.github.turskyi.hydrationreminder.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import io.github.turskyi.hydrationreminder.sync.WaterReminderIntentService.Companion.JOB_ID

/**
 * A receiver that acts as a pass-through for enqueueing work to a [JobIntentService].
 * @see [https://stackoverflow.com/a/49294691/10636137]
 */
class StartJobIntentServiceReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_SERVICE_CLASS = "io.github.turskyi.hydrationreminder.sync.WaterReminderIntentService"
        const val EXTRA_JOB_ID = "io.github.turskyi.hydrationreminder.extra_job_id"

        /**
         * @param intent an Intent meant for a [JobIntentService]
         * @return a new Intent intended for use by this receiver based off the passed intent
         */
        fun getIntent(context: Context, intent: Intent, job_id: Int): Intent {
            val component = intent.component ?: throw RuntimeException("Missing intent component")
            val newIntent = Intent(intent)
                .putExtra(EXTRA_SERVICE_CLASS, component.className)
                .putExtra(EXTRA_JOB_ID, job_id)
            newIntent.setClass(context, StartJobIntentServiceReceiver::class.java)
            return newIntent
        }
    }

   override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.extras == null) throw Exception("No extras found")

            val serviceClassName = intent.getStringExtra(EXTRA_SERVICE_CLASS)
                ?: throw Exception("No service class found in extras")
            val serviceClass = Class.forName(serviceClassName)
            if (!JobIntentService::class.java.isAssignableFrom(serviceClass)) throw Exception("Service class found is not a JobIntentService: " + serviceClass.name)
            intent.setClass(context, serviceClass)


            /* getting job id */
            if (intent.extras?.containsKey(EXTRA_JOB_ID) == false) throw Exception("No job ID found in extras")
            val jobId = intent.getIntExtra(EXTRA_JOB_ID, JOB_ID)


            /* starting the service */
            JobIntentService.enqueueWork(context, serviceClass, jobId, intent)
        } catch (e: Exception) {
            System.err.println("Error starting service from receiver: " + e.message)
        }
    }
}