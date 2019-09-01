package com.github.ericytsang.app.dynamicforms.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun <R> LiveData<R>.awaitValue():R
{
    val q = ArrayBlockingQueue<List<R>>(1)
    observeForever(object:Observer<R>
    {
        override fun onChanged(t:R)
        {
            q.add(listOf(t))
            removeObserver(this)
        }
    })
    val result = q.poll(2,TimeUnit.SECONDS)
        ?: throw TimeoutException("waited too long for value")
    return result.single()
}
