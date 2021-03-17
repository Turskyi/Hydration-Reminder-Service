package io.github.turskyi.hydrationreminder.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderUtilities {
    /*
     * Interval at which to remind the user to drink water. Use TimeUnit for convenience, rather
     * than writing out a bunch of multiplication ourselves and risk making a silly mistake.
     */
    private const val REMINDER_INTERVAL_MINUTES = 15
    private const val REMINDER_JOB_TAG = "hydration_reminder_tag"
    private const val NOTIFICATION_WORK = "notification_work"

    @Synchronized
    fun scheduleChargingReminder(context: Context) {
        val workManager: WorkManager = WorkManager.getInstance(context)
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        val currentTime: Calendar = Calendar.getInstance()
        val dueTime: Calendar = Calendar.getInstance()
        dueTime.add(Calendar.MINUTE, REMINDER_INTERVAL_MINUTES)
        val timeDiff: Long = dueTime.timeInMillis - currentTime.timeInMillis

        /* Create the Job to periodically create reminders to drink water */
        val notificationFirstTimeRequest: OneTimeWorkRequest = OneTimeWorkRequest
            /* The Service that will be used to write to preferences */
            .Builder(WaterReminderWork::class.java)
            /*
        * We want the reminders to happen every 15 minutes or so. The first argument for
        * Trigger class's static executionWindow method is the start of the time frame
        * when the
        * job should be performed. The second argument is the latest point in time at
        * which the data should be synced. Please note that this end time is not
        * guaranteed, but is more of a guideline for FirebaseJobDispatcher to go off of.
        */
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            /*
             * Set the UNIQUE tag used to identify this Job.
             */
            .addTag(REMINDER_JOB_TAG)
            /*
             * Network constraints on which this Job should run. In this app, we're using the
             * device charging constraint so that the job only executes if the device is
             * charging.
             *
             * In a normal app, it might be a good idea to include a preference for this,
             * as different users may have different preferences on when you should be
             * syncing your application's data.
             */
            .setConstraints(constraints)
            /* Once the Job is ready, call the builder's build method to return the Job */
            .build()
        /*
                 * If a Job with the tag with provided already exists, this new job will replace
                 * the old one.
                 */
        workManager.enqueueUniqueWork(
            NOTIFICATION_WORK,
            ExistingWorkPolicy.REPLACE,
            notificationFirstTimeRequest
        )
    }
}