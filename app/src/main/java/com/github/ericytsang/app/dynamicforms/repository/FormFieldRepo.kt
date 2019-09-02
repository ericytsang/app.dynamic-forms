package com.github.ericytsang.app.dynamicforms.repository

import com.github.ericytsang.app.dynamicforms.database.AppDatabase
import com.github.ericytsang.app.dynamicforms.database.DateFormFieldEntity
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.database.TextFormFieldEntity
import java.util.Calendar

class FormFieldRepo(
    private val db:AppDatabase
)
{
    private val formFieldDao = db.formFieldDao()
    private val formFieldEntitySubclassDaos = listOf(
        db.textFormFieldDao(),
        db.dateFormFieldDao()
    )

    fun getAllForForm(formPk:FormEntity.Pk):List<FormField>
    {
        val formFieldsByPk = formFieldDao
            .selectAllForForm(formPk)
            .associateBy {it.pk}
        return formFieldEntitySubclassDaos
            .flatMap {it.selectAllForForm(formPk)}
            .map {
                val parentFormField = formFieldsByPk[it.pk] ?: error("no corresponding parent form field found")
                when (it)
                {
                    is TextFormFieldEntity -> FormField.TextFormField(
                        it.pk,
                        FormField.TextFormField.Values(
                            FormEntity.Pk(parentFormField.values.formId),
                            parentFormField.values.positionInForm,
                            parentFormField.values.isRequired,
                            it.values.value
                        )
                    )
                    is DateFormFieldEntity -> FormField.DateFormField(
                        it.pk,
                        FormField.DateFormField.Values(
                            FormEntity.Pk(parentFormField.values.formId),
                            parentFormField.values.positionInForm,
                            parentFormField.values.isRequired,
                            Calendar.getInstance().apply {timeInMillis = it.values.value}
                        )
                    )
                }
            }
            .sortedBy {it.values.positionInForm}
    }
}
