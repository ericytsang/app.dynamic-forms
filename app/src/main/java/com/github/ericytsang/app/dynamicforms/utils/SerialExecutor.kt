package com.github.ericytsang.app.dynamicforms.utils

import android.os.AsyncTask
import android.text.Editable
import android.text.TextWatcher

class SerialExecutor
{
    fun execute(asyncTask:AsyncTask<*,*,*>)
    {
        asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
    }
}

interface TextWatcherAdapter:TextWatcher
{
    override fun afterTextChanged(s:Editable?) = Unit
    override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int) = Unit
    override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int) = Unit
}