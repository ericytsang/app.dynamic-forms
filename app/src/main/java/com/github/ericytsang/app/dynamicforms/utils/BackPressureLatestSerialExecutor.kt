package com.github.ericytsang.app.dynamicforms.utils

import android.os.AsyncTask
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class BackPressureLatestSerialExecutor
{
    private val lock = ReentrantLock()
    private var previousTask:AsyncTask<*,*,*>? = null

    fun execute(asyncTask:AsyncTask<*,*,*>)
    {
        lock.withLock()
        {
            previousTask?.cancel(false)
            previousTask = asyncTask
            asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
        }
    }
}