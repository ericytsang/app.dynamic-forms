package com.github.ericytsang.app.dynamicforms.viewmodel

import android.content.Context
import android.os.AsyncTask
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.RoomDatabase
import com.github.ericytsang.app.dynamicforms.FormFieldViewHolderModel
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
import com.github.ericytsang.app.dynamicforms.viewmodel.AsyncTaskBuilder.Companion.build

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

@MainThread
class MainActivityViewModel(
    private val db:RoomDatabase,
    private val formRepo:FormRepo,
    private val formFieldRepo:FormFieldRepo,
    private val imageUrlFactory:ImageUrlFactory
):
    ViewModel()
{
    companion object
    {
        private data class FactoryParams(
            val db:RoomDatabase,
            val formRepo:FormRepo,
            val formFieldRepo:FormFieldRepo,
            val imageUrlFactory:ImageUrlFactory
        )

        private val factory = SingletonFactory()
        {params:FactoryParams ->
            MainActivityViewModel(
                params.db,
                params.formRepo,
                params.formFieldRepo,
                params.imageUrlFactory
            )
        }

        fun getInstance(
            db:RoomDatabase,
            formRepo:FormRepo,
            formFieldRepo:FormFieldRepo,
            imageUrlFactory:ImageUrlFactory
        ):MainActivityViewModel
        {
            return factory.getInstance(FactoryParams(db,formRepo,formFieldRepo,imageUrlFactory))
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
                    ?:listOf()
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
            val unsavedChanges:List<FormFieldViewHolderModel>
        ):
            FormDetailState()
        {
            init
            {
                require(unsavedChanges == unsavedChanges.sortedBy {it.positionInForm})
            }

            val hasBeenModified:Boolean get()
            {
                return original.formPk == null || original.formFields != unsavedChanges
            }
        }
    }

    data class FormDetails(
        val formPk:FormEntity.Pk?,
        val imageUrl:Url,
        val formFields:List<FormFieldViewHolderModel>
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
                                .map {formField -> FormFieldViewHolderModel.fromModel(formField.values)}
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
    fun publishPendingChanges(updatedFormField:FormFieldViewHolderModel)
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

    fun openNewFormForEditing(context:Context)
    {
        backPressureLatestSerialExecutor.execute(
            asyncTaskBuilder()
                .preExecute {/* todo show that we're loading */}
                .background {
                    /* todo fetch from network */
                    val formFields = listOf(
                        FormFieldViewHolderModel.Text(0,"Hello World!",false,"initial value"),
                        FormFieldViewHolderModel.Text(1,"is it finally working?",false,""),
                        FormFieldViewHolderModel.Text(2,"let's see...",false,"initial value")
                    )
                    FormDetailState.Edit(
                        FormDetails(
                            null,
                            imageUrlFactory.make(),
                            formFields
                        ),
                        formFields
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

fun asyncTaskBuilder() = AsyncTaskBuilder<Nothing?,Nothing?>({},{null},{})

class StrategicAsyncTask<R>(
    private val preExecute:()->Unit = {},
    private val background:()->R,
    private val postExecute:(R)->Unit = {}
):
    AsyncTask<Void,Int,R>()
{
    override fun onPreExecute() = preExecute()
    override fun doInBackground(vararg params:Void?):R = background()
    override fun onPostExecute(result:R) = postExecute(result)
}

class AsyncTaskBuilder<BackgroundResult,UiParams>(
    private val preExecute:()->Unit,
    private val doInBackground:()->BackgroundResult,
    private val postExecute:(UiParams)->Unit
)
{
    companion object
    {
        fun <X> AsyncTaskBuilder<X,X>.build() = StrategicAsyncTask(
            preExecute,doInBackground,postExecute
        )
    }

    fun <T> background(doInBackground:()->T) =
        AsyncTaskBuilder(preExecute,doInBackground,postExecute)

    fun preExecute(preExecute:()->Unit) = AsyncTaskBuilder(preExecute,doInBackground,postExecute)
    fun postExecute(postExecute:(BackgroundResult)->Unit) =
        AsyncTaskBuilder(preExecute,doInBackground,postExecute)
}