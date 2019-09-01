package com.github.ericytsang.app.dynamicforms.viewmodel

import android.os.AsyncTask
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.utils.SingletonFactory
import com.github.ericytsang.app.dynamicforms.repository.Form
import com.github.ericytsang.app.dynamicforms.repository.FormRepo
import com.github.ericytsang.app.dynamicforms.repository.Url

@MainThread
class MainActivityViewModel(
    private val formRepo:FormRepo
):
    ViewModel()
{
    companion object
    {
        val factory = SingletonFactory {formRepo:FormRepo -> MainActivityViewModel(formRepo)}
    }

    val sortingMode = MutableLiveData<SortingMode>()
        .apply {value = SortingMode.CREATE_DATE_DESCENDING}

    private val _selectedListItems =
        MutableLiveData<MutableSet<FormEntity.Pk>>()
            .apply {value = mutableSetOf()}
    val selectedListItems:LiveData<Set<FormEntity.Pk>> = _selectedListItems
        .map {it as Set<FormEntity.Pk>}

    val formListItems:LiveData<List<Form>> = sortingMode
        .switchMap {_:SortingMode -> /* todo: use sorting mode */formRepo.getAllForms()}

    fun addToMultiSelection(formPk:FormEntity.Pk)
    {
        _selectedListItems.value?.add(formPk)
        _selectedListItems.value = _selectedListItems.value
    }

    fun removeFromMultiSelection(formPk:FormEntity.Pk)
    {
        _selectedListItems.value?.remove(formPk)
        _selectedListItems.value = _selectedListItems.value
    }

    fun clearMultiSelection()
    {
        _selectedListItems.value?.clear()
        _selectedListItems.value = _selectedListItems.value
    }

    fun addRandomForm()
    {
        AsyncTask.THREAD_POOL_EXECUTOR.execute()
        {
            formRepo.create(
                Form.Values(
                    Url("https://github.com"),
                    "title",
                    "description"
                )
            )
        }
    }

    enum class SortingMode
    {
        CREATE_DATE_ASCENDING,
        CREATE_DATE_DESCENDING,
        LEXICAL_ASCENDING,
        LEXICAL_DESCENDING,
    }
}
