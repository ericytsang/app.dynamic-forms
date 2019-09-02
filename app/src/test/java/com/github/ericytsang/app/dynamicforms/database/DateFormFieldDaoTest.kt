package com.github.ericytsang.app.dynamicforms.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.ericytsang.app.dynamicforms.utils.DbTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DateFormFieldDaoTest
{
    @get:Rule
    val dbTestRule = DbTestRule(
        InstrumentationRegistry.getInstrumentation().targetContext,
        AppDatabase::class.java
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val formDao = dbTestRule.database.formDao()
    private val formFieldDao = dbTestRule.database.formFieldDao()
    private val dateFormFieldDao = dbTestRule.database.dateFormFieldDao()

    private val testForms = listOf(
        FormEntity.Values("url","Title#1","Description#1"),
        FormEntity.Values("url","Title#2","Description#2"),
        FormEntity.Values("url","Title#3","Description#3")
    )
        .map {FormEntity(formDao.insert(it),it)}

    private val testFormFields = testForms
        .flatMap {parentForm ->
            listOf(
                FormFieldEntity.Values(parentForm.pk.id,1,true),
                FormFieldEntity.Values(parentForm.pk.id,2,false),
                FormFieldEntity.Values(parentForm.pk.id,3,false)
            )
        }
        .map {FormFieldEntity(formFieldDao.insert(it),it)}

    private val testDateFormFields = testFormFields
        .map {parentFormField ->
            DateFormFieldEntity(parentFormField.pk,DateFormFieldEntity.Values(128))
                .also {dateFormFieldDao.insert(it)}
        }

    @Test
    fun selectAllForForm_returns_all_rows_for_form()
    {
        val parentForm = testForms[1]
        val parentFormFieldIds = testFormFields
            .filter {it.values.formId == parentForm.pk.id}
            .map {it.pk}
            .toSet()
        assertEquals(
            testDateFormFields
                .filter {it.pk in parentFormFieldIds}
                .toSet(),
            dateFormFieldDao
                .selectAllForForm(parentForm.pk)
                .toSet()
        )
    }

    @Test
    fun deleting_parent_FormEntity_deletes_transient_child_DateFormFieldEntity_rows()
    {
        val parentFormToDeletePk = testForms[1].pk
        formDao.delete(parentFormToDeletePk)
        assertEquals(
            testFormFields
                .filter {it.values.formId != parentFormToDeletePk.id}
                .map {it.pk}
                .toSet(),
            dateFormFieldDao
                .selectAll()
                .map {it.pk}
                .toSet()
        )
    }

    @Test
    fun update_updates_a_row()
    {
        val toUpdate = testDateFormFields[2].run()
        {
            DateFormFieldEntity(pk,values.copy(value = 24234))
        }
        dateFormFieldDao.update(toUpdate)
        assertEquals(toUpdate,dateFormFieldDao.selectOne(toUpdate.pk))
    }

    @Test
    fun cannot_insert_a_row_with_dangling_parent_FormFieldEntity_fk()
    {
        val danglingFormFieldId = FormFieldEntity.Pk(50)
        check(danglingFormFieldId !in testFormFields.map {it.pk})
        val toInsert = DateFormFieldEntity(danglingFormFieldId,DateFormFieldEntity.Values(100))
        val insertResult = runCatching()
        {
            dateFormFieldDao.insert(toInsert)
        }
        assertNotNull(insertResult.exceptionOrNull())
    }
}
