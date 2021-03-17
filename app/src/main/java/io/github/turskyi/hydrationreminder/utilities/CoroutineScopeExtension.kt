package io.github.turskyi.hydrationreminder.utilities

import kotlinx.coroutines.*

fun <R> CoroutineScope.executeAsyncTask(
    onPreExecute: (() -> Unit)? = null,
    doInBackground: () -> R,
    onPostExecute: ((R) -> Unit)? = null
): Job {
    return launch {
        onPreExecute?.invoke()
        /* runs in background thread without blocking the Main Thread */
        val result = withContext(Dispatchers.IO) {
            doInBackground()
        }
        onPostExecute?.invoke(result)
    }
}