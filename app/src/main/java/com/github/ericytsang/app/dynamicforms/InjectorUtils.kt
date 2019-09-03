package com.github.ericytsang.app.dynamicforms

import android.content.Context
import com.github.ericytsang.app.dynamicforms.database.AppDatabase
import com.github.ericytsang.app.dynamicforms.domainobjects.Url
import com.github.ericytsang.app.dynamicforms.repository.FormFieldRepo
import com.github.ericytsang.app.dynamicforms.repository.FormRepo
import com.github.ericytsang.app.dynamicforms.viewmodel.MainActivityViewModel
import com.github.ericytsang.app.dynamicforms.viewmodel.NewFormDataFactory
import com.github.ericytsang.app.dynamicforms.viewmodel.RoundRobinUrlDownloadingNewFormDataFactory
import com.github.ericytsang.app.dynamicforms.viewmodel.RoundRobinUrlDownloadingNewFormDataFactory.Companion.Params

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

    private fun getFormFieldsFactory(context:Context):NewFormDataFactory
    {
        return RoundRobinUrlDownloadingNewFormDataFactory.factory.getInstance(
            Params(
                listOf(
                    Url("https://raw.githubusercontent.com/ericytsang/app.dynamic-forms/master/.api/form1.json"),
                    Url("https://raw.githubusercontent.com/ericytsang/app.dynamic-forms/master/.api/form2.json")
                ),
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
            getFormFieldsFactory(context)
        )
    }
}
