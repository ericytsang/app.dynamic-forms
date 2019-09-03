package com.github.ericytsang.app.dynamicforms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.utils.ActivityCompanion
import com.github.ericytsang.app.dynamicforms.utils.SingletonFactory
import com.github.ericytsang.app.dynamicforms.viewmodel.FormDetailFragmentViewModel
import com.github.ericytsang.app.dynamicforms.viewmodel.FormDetailFragmentViewModelFactory

class FormActivity:AppCompatActivity(),FormDetailFragmentViewModelFactory
{
    companion object:
        ActivityCompanion<FormActivity,FormEntity.Pk>()
    {
        override fun paramsClass() = FormEntity.Pk::class
        override fun activityClass() = FormActivity::class
        override fun flags(params:FormEntity.Pk) = Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    private val factory = SingletonFactory()
    {
        context:Context->
        InjectorUtils.newFormDetailFragmentViewModel(context)
    }

    override fun getFormDetailFragmentViewModel():FormDetailFragmentViewModel
    {
        return factory.getInstance(this)
    }

    override fun onCreate(savedInstanceState:Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__form)

        getFormDetailFragmentViewModel().selectOne(toParams(intent))
    }
}
