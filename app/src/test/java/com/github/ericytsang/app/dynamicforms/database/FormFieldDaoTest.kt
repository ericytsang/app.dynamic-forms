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
class FormFieldDaoTest
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

    @Test
    fun selectAllForForm_returns_all_rows_for_form()
    {
        val parentForm = testForms[1]
        assertEquals(
            testFormFields
                .filter {it.values.formId == parentForm.pk.id}
                .toSet(),
            formFieldDao
                .selectAllForForm(parentForm.pk)
                .toSet()
        )
    }

    @Test
    fun deleting_parent_FormEntity_deletes_child_FormFieldEntity_rows()
    {
        val parentFormToDeletePk = testForms[1].pk
        formDao.delete(parentFormToDeletePk)
        assertEquals(
            testFormFields
                .filter {it.values.formId != parentFormToDeletePk.id}
                .map {it.pk}
                .toSet(),
            formFieldDao
                .selectAll()
                .map {it.pk}
                .toSet()
        )
    }

    @Test
    fun update_updates_a_row()
    {
        val toUpdate = testFormFields[2].run()
        {
            FormFieldEntity(pk,values.copy(positionInForm = 4))
        }
        formFieldDao.update(toUpdate)
        assertEquals(toUpdate,formFieldDao.selectOne(toUpdate.pk))
    }

    @Test
    fun insert_adds_a_row()
    {
        val parentForm = testForms[1]
        val toInsert = FormFieldEntity.Values(parentForm.pk.id,4,false)
        val insertedRowId = formFieldDao.insert(toInsert)
        val insertedRow = FormFieldEntity(insertedRowId,toInsert)
        assertEquals(insertedRow,formFieldDao.selectOne(insertedRowId))
        assertEquals(
            testFormFields.plus(insertedRow).toSet(),
            formFieldDao.selectAll().toSet())
    }

    @Test
    fun cannot_insert_a_row_with_dangling_parent_FormEntity_fk()
    {
        val danglingFormId = 4L
        check(danglingFormId !in testForms.map {it.pk.id})
        val toInsert = FormFieldEntity.Values(danglingFormId,4,false)
        val insertResult = runCatching()
        {
            formFieldDao.insert(toInsert)
        }
        assertNotNull(insertResult.exceptionOrNull())
    }
}
