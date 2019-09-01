package com.github.ericytsang.app.dynamicforms

import android.app.Activity
import android.content.Context
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import java.io.File


// Context

val Context.layoutInflater get() = LayoutInflater.from(this)

// Activity

val Activity.contentView get() = findViewById<ViewGroup>(android.R.id.content)
