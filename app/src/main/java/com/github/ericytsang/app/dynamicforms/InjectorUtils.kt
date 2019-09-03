package com.github.ericytsang.app.dynamicforms

import android.content.Context
import com.github.ericytsang.app.dynamicforms.database.AppDatabase
import com.github.ericytsang.app.dynamicforms.domainobjects.Url
import com.github.ericytsang.app.dynamicforms.repository.FormFieldRepo
import com.github.ericytsang.app.dynamicforms.repository.FormRepo
import com.github.ericytsang.app.dynamicforms.viewmodel.MainActivityViewModel
import com.github.ericytsang.app.dynamicforms.newformdatafactory.NewFormDataFactory
import com.github.ericytsang.app.dynamicforms.newformdatafactory.RoundRobinUrlDownloadingNewFormDataFactory
import com.github.ericytsang.app.dynamicforms.newformdatafactory.RoundRobinUrlDownloadingNewFormDataFactory.Companion.Params
import com.github.ericytsang.app.dynamicforms.viewmodel.FormDetailFragmentViewModel

object InjectorUtils
{
    private fun getAppDatabaseInstance(context:Context):AppDatabase
    {
        return AppDatabase.factory.getInstance(context.applicationContext)
    }

    private fun getFormRepoInstance(context:Context):FormRepo
    {
        return FormRepo(getAppDatabaseInstance(context))
    }

    private fun getFormFieldRepoInstance(context:Context):FormFieldRepo
    {
        return FormFieldRepo(getAppDatabaseInstance(context))
    }

    private fun getFormFieldsFactoryInstance(context:Context):NewFormDataFactory
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

    fun getMainActivityViewModelInstance(context:Context):MainActivityViewModel
    {
        return MainActivityViewModel.getInstance(
            getAppDatabaseInstance(context),
            getFormRepoInstance(context),
            getFormFieldRepoInstance(context),
            getFormFieldsFactoryInstance(context)
        )
    }

    fun newFormDetailFragmentViewModel(context:Context):FormDetailFragmentViewModel
    {
        return FormDetailFragmentViewModel(
            getAppDatabaseInstance(context),
            getFormRepoInstance(context),
            getFormFieldRepoInstance(context),
            getFormFieldsFactoryInstance(context)
        )
    }
}
