package com.github.ericytsang.app.dynamicforms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.domainobjects.Url
import com.github.ericytsang.app.dynamicforms.utils.ActivityCompanion
import com.github.ericytsang.app.dynamicforms.utils.SingletonFactory
import com.github.ericytsang.app.dynamicforms.utils.exhaustive
import com.github.ericytsang.app.dynamicforms.viewmodel.FormDetailFragmentViewModel
import com.github.ericytsang.app.dynamicforms.viewmodel.FormDetailFragmentViewModelFactory
import java.io.Serializable


class FormActivity:AppCompatActivity(),FormDetailFragmentViewModelFactory
{
    companion object:ActivityCompanion<FormActivity,Params>()
    {
        override fun paramsClass() = Params::class
        override fun activityClass() = FormActivity::class
        override fun flags(params:Params) = Intent.FLAG_ACTIVITY_CLEAR_TOP

        private val factory = SingletonFactory()
        {
                context:Context->
            InjectorUtils.newFormDetailFragmentViewModel(context)
        }

        private fun getFormDetailFragmentViewModel(context:Context):FormDetailFragmentViewModel
        {
            return factory.getInstance(context)
        }
    }

    sealed class Params:Serializable
    {
        class New:Params()
        data class Edit(val formPk:FormEntity.Pk):Params()
    }

    override fun getFormDetailFragmentViewModel():FormDetailFragmentViewModel
    {
        return getFormDetailFragmentViewModel(this)
    }

    override fun onCreate(savedInstanceState:Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__form)

        if (savedInstanceState == null)
        {
            val params = toParams(intent)
            when (params)
            {
                is Params.New -> getFormDetailFragmentViewModel().openNewFormForEditing()
                is Params.Edit -> getFormDetailFragmentViewModel().selectOne(params.formPk)
            }.exhaustive
        }
    }
}
