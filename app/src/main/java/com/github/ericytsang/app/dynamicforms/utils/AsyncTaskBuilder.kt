package com.github.ericytsang.app.dynamicforms.utils

import android.os.AsyncTask


fun asyncTaskBuilder() =
    AsyncTaskBuilder<Nothing?,Nothing?>({},{null},{})

class StrategicAsyncTask<R>(
    private val preExecute:()->Unit = {},
    private val background:()->R,
    private val postExecute:(R)->Unit = {}
):
    AsyncTask<Void,Int,R>()
{
    override fun onPreExecute() = preExecute()
    override fun doInBackground(vararg params:Void?):R = background()
    override fun onPostExecute(result:R) = postExecute(result)
}

class AsyncTaskBuilder<BackgroundResult,UiParams>(
    private val preExecute:()->Unit,
    private val doInBackground:()->BackgroundResult,
    private val postExecute:(UiParams)->Unit
)
{
    companion object
    {
        fun <X> AsyncTaskBuilder<X,X>.build() =
            StrategicAsyncTask(
                preExecute,
                doInBackground,
                postExecute
            )
    }

    fun <T> background(doInBackground:()->T) =
        AsyncTaskBuilder(
            preExecute,
            doInBackground,
            postExecute
        )

    fun preExecute(preExecute:()->Unit) =
        AsyncTaskBuilder(
            preExecute,
            doInBackground,
            postExecute
        )

    fun postExecute(postExecute:(BackgroundResult)->Unit) =
        AsyncTaskBuilder(
            preExecute,
            doInBackground,
            postExecute
        )
}