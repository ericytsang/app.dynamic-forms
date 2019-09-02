package com.github.ericytsang.app.dynamicforms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FormActivity:AppCompatActivity()
{
    companion object
    {
        fun start(context:Context)
        {
            context.startActivity(Intent(context,FormActivity::class.java).apply()
            {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
    }

    override fun onCreate(savedInstanceState:Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__form)
    }
}
