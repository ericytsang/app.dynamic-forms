package com.github.ericytsang.app.dynamicforms.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.github.ericytsang.app.dynamicforms.database.AppDatabase
import com.github.ericytsang.app.dynamicforms.database.FormEntity

class FormRepo(
    private val db:AppDatabase
)
{
    private val dao = db.formDao()

    fun getAllForms():LiveData<List<Form>>
    {
        return dao
            .selectAll()
            .map()
            {forms ->
                forms.map()
                {
                    Form(
                        it.pk,
                        Form.Values(
                            Url(
                                it.values.imageUrl
                            ),
                            it.values.title,
                            it.values.description
                        )
                    )
                }
            }
    }

    fun delete(pk:FormEntity.Pk)
    {
        dao.delete(pk)
    }

    fun create(formValues:Form.Values)
    {
        dao.insert(
            FormEntity.Values(
                formValues.imageUrl.url,
                formValues.title,
                formValues.description
            )
        )
    }

    fun update(form:Form)
    {
        dao.insert(
            FormEntity.Values(
                form.values.imageUrl.url,
                form.values.title,
                form.values.description
            )
        )
    }
}
