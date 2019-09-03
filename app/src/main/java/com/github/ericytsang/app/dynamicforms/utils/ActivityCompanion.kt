package com.github.ericytsang.app.dynamicforms.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

abstract class ActivityCompanion<A:Activity,P:Serializable>
{
    companion object
    {
        private const val INTENT_EXTRA_KEY = "INTENT_EXTRA_KEY"
    }

    abstract fun paramsClass():KClass<P>
    abstract fun activityClass():KClass<A>
    abstract fun flags(params:P):Int

    fun start(context:Context,params:P)
    {
        context.startActivity(
            Intent(context,activityClass().java).apply()
        {
            flags = flags(params)
            putExtra(INTENT_EXTRA_KEY,params)
        })
    }

    protected fun toParams(intent:Intent):P
    {
        return paramsClass().cast(intent.getSerializableExtra(INTENT_EXTRA_KEY))
    }
}