package io.github.turskyi.hydrationreminder.sync

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.github.turskyi.hydrationreminder.sync.ReminderTasks.executeTask
import io.github.turskyi.hydrationreminder.utilities.executeAsyncTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class WaterReminderWork(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    private val workScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    /**
     * The entry point to this Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    override fun doWork(): Result {
        return try {
            workScope.executeAsyncTask(doInBackground = {
                executeTask(applicationContext, ReminderTasks.ACTION_CHARGING_REMINDER)
            },
                onPostExecute = {
                    ReminderUtilities.scheduleChargingReminder(applicationContext)
                })
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     */
    override fun onStopped() {
        super.onStopped()
        workScope.cancel()
    }
}