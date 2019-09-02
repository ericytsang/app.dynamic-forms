package com.github.ericytsang.app.dynamicforms

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat


// Context

val Context.layoutInflater get() = LayoutInflater.from(this)

fun Context.getDrawableCompat(@DrawableRes drawableResId:Int):Drawable
{
    return ContextCompat.getDrawable(this,drawableResId)!!
}

fun Context.getColorCompat(@ColorRes colorResId:Int):Int
{
    return ContextCompat.getColor(this,colorResId)
}

// Activity

val Activity.contentView get() = findViewById<ViewGroup>(android.R.id.content)

// force exhaustive when

val <R> R.exhaustive:R get() = this

// general

inline fun <reified T> T.debugLog(lazyMessage:()->String)
{
    if (BuildConfig.DEBUG)
    {
        Log.d(T::class.simpleName?:"<no name>",lazyMessage())
    }
}
