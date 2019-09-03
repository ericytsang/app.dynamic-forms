package com.github.ericytsang.app.dynamicforms.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.github.ericytsang.app.dynamicforms.database.AppDatabase
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.domainobjects.Form


class FormRepo(
    private val db:AppDatabase
)
{
    private val dao = db.formDao()

    fun getAll():LiveData<List<Form>>
    {
        return dao
            .selectAll()
            .map()
            {forms ->
                forms.map {Form.fromEntity(it)}
            }
    }

    fun getOne(formPk:FormEntity.Pk):Form?
    {
        return dao
            .selectOne(formPk)
            ?.let {Form.fromEntity(it)}
    }

    fun delete(pk:FormEntity.Pk)
    {
        dao.delete(pk)
    }

    fun create(formValues:Form.Values):FormEntity.Pk
    {
        return dao.insert(formValues.toEntity())
    }

    fun create(form:Form)
    {
        dao.insert(form.toEntity())
    }
}
