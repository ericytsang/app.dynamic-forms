package com.github.ericytsang.app.dynamicforms.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.RoomDatabase
import com.github.ericytsang.app.dynamicforms.FormViewHolderModel
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.domainobjects.Form
import com.github.ericytsang.app.dynamicforms.repository.FormFieldRepo
import com.github.ericytsang.app.dynamicforms.repository.FormRepo
import com.github.ericytsang.app.dynamicforms.utils.AsyncTaskBuilder.Companion.build
import com.github.ericytsang.app.dynamicforms.utils.SerialExecutor
import com.github.ericytsang.app.dynamicforms.utils.SingletonFactory
import com.github.ericytsang.app.dynamicforms.utils.asyncTaskBuilder

@MainThread
class MainActivityViewModel(
    private val db:RoomDatabase,
    private val formRepo:FormRepo,
    private val formFieldRepo:FormFieldRepo,
    private val newFormDataFactory:NewFormDataFactory
):
    ViewModel()
{
    companion object
    {
        private data class FactoryParams(
            val db:RoomDatabase,
            val formRepo:FormRepo,
            val formFieldRepo:FormFieldRepo,
            val newFormDataFactory:NewFormDataFactory
        )

        private val factory = SingletonFactory()
        {params:FactoryParams ->
            MainActivityViewModel(
                params.db,
                params.formRepo,
                params.formFieldRepo,
                params.newFormDataFactory
            )
        }

        fun getInstance(
            db:RoomDatabase,
            formRepo:FormRepo,
            formFieldRepo:FormFieldRepo,
            newFormDataFactory:NewFormDataFactory
        ):MainActivityViewModel
        {
            return factory.getInstance(FactoryParams(db,formRepo,formFieldRepo,newFormDataFactory))
        }
    }


    private val serialExecutor = SerialExecutor()


    // single-select or multi-select forms in list

    val listItemSelection:LiveData<FormEntity.Pk?> get() = _listItemSelection
    private val _listItemSelection = MutableLiveData<FormEntity.Pk?>()

    fun selectOne(formPk:FormEntity.Pk?)
    {
        _listItemSelection.value = formPk
    }


    // list of forms

    val sortingMode = MutableLiveData<SortingMode>()
        .apply {value = SortingMode.CREATE_DATE_DESCENDING}

    // todo: add empty & loading states
    val formList:LiveData<List<FormViewHolderModel>> = run()
    {
        val combined = MediatorLiveData<List<FormViewHolderModel>>()

        data class Data(
            val forms:List<Form>?,
            val sortingMode:SortingMode?,
            val selection:FormEntity.Pk?
        )
        {
            fun combine():Data
            {
                sortingMode // todo use to determine how we query the formRepo
                val selectedItems = listOfNotNull(selection).toSet()
                combined.value = forms
                    ?.map {
                        FormViewHolderModel(
                            it,
                            it.pk in selectedItems,
                            null/* todo set the bitmap */
                        )
                    }
                    ?: listOf()
                return this
            }
        }

        val formList = formRepo.getAll()
        var data = Data(null,null,null)
        combined.apply()
        {
            addSource(formList)
            {
                data = data.copy(forms = it).combine()
            }
            addSource(sortingMode)
            {
                data = data.copy(sortingMode = it).combine()
            }
            addSource(listItemSelection)
            {
                data = data.copy(selection = it).combine()
            }
        }
    }

    fun delete(toDelete:FormEntity.Pk)
    {
        serialExecutor.execute(
            asyncTaskBuilder()
                .background {
                    formRepo.delete(toDelete)
                }
                .postExecute {}
                .build())
    }

    enum class SortingMode
    {
        CREATE_DATE_ASCENDING,
        CREATE_DATE_DESCENDING,
        LEXICAL_ASCENDING,
        LEXICAL_DESCENDING,
    }


    // view model for the form details fragment (list of form fields)

    val formDetailFragmentViewModel =
        FormDetailFragmentViewModel(
            db,
            formRepo,
            formFieldRepo,
            newFormDataFactory
        )

    // when the form selection changes on this screen, it also changes in the
    init
    {
        listItemSelection.observeForever()
        {
            formDetailFragmentViewModel.selectOne(it)
        }
    }
}
