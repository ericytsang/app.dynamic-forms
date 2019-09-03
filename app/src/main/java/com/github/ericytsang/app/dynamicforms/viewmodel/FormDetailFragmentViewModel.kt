package com.github.ericytsang.app.dynamicforms.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.RoomDatabase
import com.github.ericytsang.app.dynamicforms.R
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.domainobjects.Form
import com.github.ericytsang.app.dynamicforms.domainobjects.FormFieldReadData
import com.github.ericytsang.app.dynamicforms.domainobjects.Url
import com.github.ericytsang.app.dynamicforms.repository.FormFieldRepo
import com.github.ericytsang.app.dynamicforms.repository.FormRepo
import com.github.ericytsang.app.dynamicforms.utils.AsyncTaskBuilder.Companion.build
import com.github.ericytsang.app.dynamicforms.utils.BackPressureLatestSerialExecutor
import com.github.ericytsang.app.dynamicforms.utils.Result
import com.github.ericytsang.app.dynamicforms.utils.SerialExecutor
import com.github.ericytsang.app.dynamicforms.utils.asyncTaskBuilder
import com.github.ericytsang.app.dynamicforms.utils.debugLog
import com.github.ericytsang.app.dynamicforms.utils.exhaustive
import com.github.ericytsang.app.dynamicforms.utils.toastLong


interface FormDetailFragmentViewModelFactory
{
    fun getFormDetailFragmentViewModel():FormDetailFragmentViewModel
}

interface MainActivityViewModelFactory
{
    fun getMainActivityViewModel():MainActivityViewModel
}



class FormDetailFragmentViewModel(
    private val db:RoomDatabase,
    private val formRepo:FormRepo,
    private val formFieldRepo:FormFieldRepo,
    private val newFormDataFactory:NewFormDataFactory
):
    ViewModel()
{
    private val serialExecutor = SerialExecutor()
    private val backPressureLatestSerialExecutor =
        BackPressureLatestSerialExecutor()


    // single-select or multi-select forms in list

    val listItemSelection:LiveData<FormEntity.Pk?> get() = _listItemSelection
    private val _listItemSelection =
        MutableLiveData<FormEntity.Pk?>()

    fun selectOne(formPk:FormEntity.Pk?)
    {
        _listItemSelection.value = formPk
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
                                .map {formField ->
                                    FormFieldReadData.fromModel(
                                        formField.values
                                    )
                                }
                            val form = formRepo.getOne(toDisplayPk)
                            if (form != null)
                            {
                                FormDetailState.Edit(
                                    FormDetails(
                                        form.pk,
                                        form.values.imageUrl,
                                        formFields
                                    ),
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




    sealed class FormDetailState
    {
        object Idle:
            FormDetailState()
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

            val canSave:Boolean
                get()
                {
                    return {false}() ||
                            // this is a new form
                            (original.formPk == null ||
                                    // or it has ben modified
                                    original.formFields != unsavedChanges) &&
                            // and all fields are valid
                            unsavedChanges.all {it.isValid}
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


    val formDetails:LiveData<FormDetailState> get() = _formDetails
    private val _formDetails = MutableLiveData<FormDetailState>()
        .apply {value =
            FormDetailState.Idle
        }

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
                    newFormDataFactory.make()
                        .mapSuccess()
                        {newFormData ->
                            FormDetailState.Edit(
                                FormDetails(
                                    null,
                                    newFormData.imageUrl,
                                    newFormData.formFields
                                ),
                                newFormData.formFields
                            )
                        }
                }
                .postExecute {
                    when (it)
                    {
                        is Result.Success -> _formDetails.value = it.success
                        is Result.Failure -> debugLog {it.failure}
                    }.exhaustive
                }
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
                        val formValues =
                            Form.Values(
                                toSave.original.imageUrl,
                                toSave.unsavedChanges.getOrNull(0)?.userInputAsString(context)
                                    ?: context.getString(R.string.main_activity_vm__no_title),
                                toSave.unsavedChanges.getOrNull(1)?.userInputAsString(context)
                                    ?: context.getString(R.string.main_activity_vm__no_description)
                            )

                        // save a new form
                        if (toSave.original.formPk == null)
                        {
                            // save form
                            val pk = formRepo.create(formValues)

                            // save form fields
                            toSave.unsavedChanges.forEach {formFieldRepo.create(it.toModel(pk))}

                            return@runInTransaction pk
                        }

                        // update an existing form otherwise
                        else
                        {
                            val pk = toSave.original.formPk

                            // delete old form
                            formRepo.delete(pk)

                            // save form
                            formRepo.create(
                                Form(
                                    pk,
                                    formValues
                                )
                            )

                            // save form fields
                            toSave.unsavedChanges.forEach()
                            {
                                formFieldRepo.create(it.toModel(pk))
                            }

                            toSave.original.formPk
                        }
                    }
                }
                .postExecute {
                    context.toastLong(R.string.main_activity_vm__saved)
                    selectOne(it)
                }
                .build())
    }
}