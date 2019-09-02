package com.github.ericytsang.app.dynamicforms.viewmodel

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.RoomDatabase
import com.github.ericytsang.app.dynamicforms.domainobjects.FormFieldReadData
import com.github.ericytsang.app.dynamicforms.FormViewHolderModel
import com.github.ericytsang.app.dynamicforms.R
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.debugLog
import com.github.ericytsang.app.dynamicforms.domainobjects.Form
import com.github.ericytsang.app.dynamicforms.domainobjects.Url
import com.github.ericytsang.app.dynamicforms.repository.FormFieldRepo
import com.github.ericytsang.app.dynamicforms.repository.FormRepo
import com.github.ericytsang.app.dynamicforms.utils.BackPressureLatestSerialExecutor
import com.github.ericytsang.app.dynamicforms.utils.SerialExecutor
import com.github.ericytsang.app.dynamicforms.utils.SingletonFactory
import com.github.ericytsang.app.dynamicforms.utils.AsyncTaskBuilder.Companion.build
import com.github.ericytsang.app.dynamicforms.utils.asyncTaskBuilder

interface ImageUrlFactory
{
    fun make():Url
}

class LoremPicsum:ImageUrlFactory
{
    override fun make():Url
    {
        return Url("https://picsum.photos/id/${(0..1084).random()}/200/200")
    }
}

/* todo fetch from network */
interface NewFormDataFactory
{
    fun make():NewFormData
}

data class NewFormData(
    val imageUrl:Url,
    val formFields:List<FormFieldReadData>
)

class DummyNewFormDataFactory:NewFormDataFactory
{
    private val loremPicsum = LoremPicsum()
    override fun make() = NewFormData(
        loremPicsum.make(),
        listOf(
            FormFieldReadData.Text(0,"Hello World!",false,"initial value"),
            FormFieldReadData.Text(1,"is it finally working?",false,""),
            FormFieldReadData.Text(2,"let's see...",false,"initial value")
        )
    )
}

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
    private val backPressureLatestSerialExecutor = BackPressureLatestSerialExecutor()


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
                    ?.map {FormViewHolderModel(it,it.pk in selectedItems)}
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


    // form screen i.e. list of form fields


    sealed class FormDetailState
    {
        object Idle:FormDetailState()
        data class Edit(
            val original:FormDetails,
            val unsavedChanges:List<FormFieldReadData>
        ):
            FormDetailState()
        {
            init
            {
                require(unsavedChanges == unsavedChanges.sortedBy {it.positionInForm})
            }

            val hasBeenModified:Boolean
                get()
                {
                    return original.formPk == null || original.formFields != unsavedChanges
                }
        }
    }

    data class FormDetails(
        val formPk:FormEntity.Pk?,
        val imageUrl:Url,
        val formFields:List<FormFieldReadData>
    )
    {
        init
        {
            require(formFields == formFields.sortedBy {it.positionInForm})
        }
    }

    init
    {
        // when the form selection changes, update the form details page to show it
        listItemSelection.observeForever()
        {toDisplayPk:FormEntity.Pk? ->

            debugLog {"formSelection: $toDisplayPk"}

            toDisplayPk ?: return@observeForever

            backPressureLatestSerialExecutor.execute(
                asyncTaskBuilder()
                    .preExecute {/* todo show that we're loading */}
                    .background {
                        db.runInTransaction<FormDetailState?>()
                        {
                            val formFields = formFieldRepo
                                .getAllForForm(toDisplayPk)
                                .map {formField -> FormFieldReadData.fromModel(formField.values)}
                            val form = formRepo.getOne(toDisplayPk)
                            if (form != null)
                            {
                                FormDetailState.Edit(
                                    FormDetails(form.pk,form.values.imageUrl,formFields),
                                    formFields
                                )
                            } else
                            {
                                FormDetailState.Idle
                            }
                        }
                    }
                    .postExecute {_formDetails.value = it}
                    .build())
        }
    }

    val formDetails:LiveData<FormDetailState> get() = _formDetails
    private val _formDetails = MutableLiveData<FormDetailState>()
        .apply {value = FormDetailState.Idle}

    /**
     * let the view model know how the form has been modified so far so that we can coordinate
     * "discard unsaved changes?" operations
     */
    fun publishPendingChanges(updatedFormField:FormFieldReadData)
    {
        _formDetails.value = when (val oldValue = _formDetails.value)
        {
            is FormDetailState.Edit -> oldValue.copy(unsavedChanges = oldValue.unsavedChanges.map()
            {
                if (it.positionInForm != updatedFormField.positionInForm) it else updatedFormField
            })
            FormDetailState.Idle,
            null -> FormDetailState.Idle
        }
    }

    fun openNewFormForEditing()
    {
        backPressureLatestSerialExecutor.execute(
            asyncTaskBuilder()
                .preExecute {/* todo show that we're loading */}
                .background {
                    val newFormData = newFormDataFactory.make()
                    FormDetailState.Edit(
                        FormDetails(
                            null,
                            newFormData.imageUrl,
                            newFormData.formFields
                        ),
                        newFormData.formFields
                    )
                }
                .postExecute {
                    _formDetails.value = it
                }
                .build())
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

    fun saveForm(context:Context,toSave:FormDetailState.Edit)
    {
        serialExecutor.execute(
            asyncTaskBuilder()
                .preExecute {/* todo show that we're loading */}
                .background {
                    db.runInTransaction<FormEntity.Pk>()
                    {
                        // delete old form
                        if (toSave.original.formPk != null)
                        {
                            formRepo.delete(toSave.original.formPk)
                        }

                        // save form
                        val pk = formRepo.create(
                            Form.Values(
                                toSave.original.imageUrl,
                                toSave.unsavedChanges.getOrNull(0)?.userInputAsString
                                    ?: context.getString(R.string.main_activity_vm__no_title),
                                toSave.unsavedChanges.getOrNull(1)?.userInputAsString
                                    ?: context.getString(R.string.main_activity_vm__no_description)
                            )
                        )

                        // save form fields
                        toSave.unsavedChanges.forEach {formFieldRepo.create(it.toModel(pk))}

                        return@runInTransaction pk
                    }
                }
                .postExecute {
                    selectOne(it)
                }
                .build())
    }

    enum class SortingMode
    {
        CREATE_DATE_ASCENDING,
        CREATE_DATE_DESCENDING,
        LEXICAL_ASCENDING,
        LEXICAL_DESCENDING,
    }
}
