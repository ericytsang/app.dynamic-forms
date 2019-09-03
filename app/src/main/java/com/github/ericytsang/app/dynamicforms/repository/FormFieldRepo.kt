package com.github.ericytsang.app.dynamicforms.repository

import com.github.ericytsang.app.dynamicforms.database.AppDatabase
import com.github.ericytsang.app.dynamicforms.database.DateFormFieldEntity
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.database.TextFormFieldEntity
import com.github.ericytsang.app.dynamicforms.domainobjects.FormField
import com.github.ericytsang.app.dynamicforms.utils.exhaustive

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

    fun save(formField:FormField)
    {
        // save FormField
        formFieldDao.insertOrUpdate(formField.toFormFieldEntity())

        // save FormFieldSubclass
        when (formField)
        {
            is FormField.TextFormField -> db.textFormFieldDao().update(formField.toTextFormFieldEntity())
            is FormField.DateFormField -> db.dateFormFieldDao().update(formField.toDateFormFieldEntity())
        }.exhaustive
    }

    fun create(formFieldValues:FormField.Values)
    {
        // save FormField
        val pk = formFieldDao.create(formFieldValues.toFormFieldEntityValues())

        // save FormFieldSubclass
        when (formFieldValues)
        {
            is FormField.Values.Text -> db.textFormFieldDao()
                .insert(TextFormFieldEntity(pk,formFieldValues.toTextFormFieldEntityValues()))
            is FormField.Values.Date -> db.dateFormFieldDao()
                .insert(DateFormFieldEntity(pk,formFieldValues.toDateFormFieldEntityValues()))
        }.exhaustive
    }
}
