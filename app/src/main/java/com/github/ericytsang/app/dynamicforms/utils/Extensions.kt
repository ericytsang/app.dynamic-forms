package com.github.ericytsang.app.dynamicforms.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.github.ericytsang.app.dynamicforms.BuildConfig
import org.json.JSONArray


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

fun Context.toastLong(@StringRes stringResId:Int)
{
    Toast.makeText(this,stringResId,Toast.LENGTH_LONG).show()
}

fun Context.toastLong(string:String)
{
    Toast.makeText(this,string,Toast.LENGTH_LONG).show()
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

// JSONArray

val JSONArray.indices get() = 0 until length()

// LiveData

fun <X> LiveData<X>.debounced():LiveData<X> = Transformations.distinctUntilChanged(this)
