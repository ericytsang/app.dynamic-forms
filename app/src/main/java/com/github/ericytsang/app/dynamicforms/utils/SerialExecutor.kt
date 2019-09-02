package com.github.ericytsang.app.dynamicforms.utils

import android.os.AsyncTask

class SerialExecutor
{
    fun execute(asyncTask:AsyncTask<*,*,*>)
    {
        asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
    }
}
