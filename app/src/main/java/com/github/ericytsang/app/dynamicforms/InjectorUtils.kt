package com.github.ericytsang.app.dynamicforms

import android.content.Context
import com.github.ericytsang.app.dynamicforms.database.AppDatabase
import com.github.ericytsang.app.dynamicforms.repository.FormFieldRepo
import com.github.ericytsang.app.dynamicforms.repository.FormRepo
import com.github.ericytsang.app.dynamicforms.viewmodel.DummyNewFormDataFactory
import com.github.ericytsang.app.dynamicforms.viewmodel.MainActivityViewModel

object InjectorUtils
{

    private fun getAppDatabase(context:Context):AppDatabase
    {
        return AppDatabase.factory.getInstance(context.applicationContext)
    }

    private fun getFormRepo(context:Context):FormRepo
    {
        return FormRepo(getAppDatabase(context))
    }

    private fun getFormFieldRepo(context:Context):FormFieldRepo
    {
        return FormFieldRepo(
            getAppDatabase(
                context
            )
        )
    }

    fun getMainActivityViewModel(context:Context):MainActivityViewModel
    {
        return MainActivityViewModel.getInstance(
            getAppDatabase(context),
            getFormRepo(context),
            getFormFieldRepo(context),
            DummyNewFormDataFactory()
        )
    }
}
