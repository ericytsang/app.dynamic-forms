package com.github.ericytsang.app.dynamicforms

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlin.reflect.KClass


// Context

val Context.layoutInflater get() = LayoutInflater.from(this)

fun Context.getDrawableCompat(drawableResId:Int):Drawable
{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
        resources.getDrawable(drawableResId,theme)
    } else
    {
        @Suppress("DEPRECATION")
        resources.getDrawable(drawableResId)
    }
}

// Activity

val Activity.contentView get() = findViewById<ViewGroup>(android.R.id.content)

// force exhaustive when

val <R> R.exhaustive:R get() = this

// general

inline fun <reified T> debugLog(lazyMessage:()->String)
{
    if (BuildConfig.DEBUG)
    {
        Log.d(T::class.simpleName?:"<no name>",lazyMessage())
    }
}
