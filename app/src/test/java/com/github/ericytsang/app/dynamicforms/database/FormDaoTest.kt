package com.github.ericytsang.app.dynamicforms.database

import androidx.test.platform.app.InstrumentationRegistry
import com.github.ericytsang.app.dynamicforms.database.FormEntity.*
import com.github.ericytsang.app.dynamicforms.utils.DbTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FormDaoTest
{
    @get:Rule
    val dbTestRule = DbTestRule(
        InstrumentationRegistry.getInstrumentation().targetContext,
        AppDatabase::class.java
    )

    private val dao = dbTestRule.database.formDao()

    private val testForms = listOf(
        Values("url","Title#1","Description#1"),
        Values("url","Title#2","Description#2"),
        Values("url","Title#3","Description#3")
    )
        .map {FormEntity(dao.insert(it),it)}

    @Test
    fun selectAll_returns_all_rows()
    {
        assertEquals(testForms,dao.selectAll())
    }

    @Test
    fun delete_deletes_a_row()
    {
        dao.delete(testForms[1].pk)
        assertEquals(
            listOf(testForms[0].pk,testForms[2].pk),
            dao.selectAll().map {it.pk}
        )
    }

    @Test
    fun update_updates_a_row()
    {
        val toUpdate = testForms[2].run()
        {
            FormEntity(pk,values.copy(imageUrl = "https://github.com"))
        }
        dao.update(toUpdate)
        assertEquals(toUpdate,dao.selectOne(toUpdate.pk))
    }

    @Test
    fun insert_adds_a_row()
    {
        val toInsert = Values("url","a different title","update")
        val insertedRowId = dao.insert(toInsert)
        val insertedRow = FormEntity(insertedRowId,toInsert)
        assertEquals(insertedRow,dao.selectOne(insertedRowId))
        assertEquals(testForms+insertedRow,dao.selectAll())
    }
}
