package com.github.ericytsang.app.dynamicforms.repository

import com.github.ericytsang.app.dynamicforms.database.AppDatabase
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.domainobjects.FormField

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
            .map {formFieldEntitySubclass ->
                val parentFormField = formFieldsByPk[formFieldEntitySubclass.pk]
                    ?: error("no corresponding parent form field found")
                FormField.fromEntities(parentFormField.values,formFieldEntitySubclass)
            }
            .sortedBy {it.values.positionInForm}
    }
}
