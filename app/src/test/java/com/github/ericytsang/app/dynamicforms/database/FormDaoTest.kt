package com.github.ericytsang.app.dynamicforms.database

import androidx.test.platform.app.InstrumentationRegistry
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
        FormValues("url","Title#1","Description#1"),
        FormValues("url","Title#2","Description#2"),
        FormValues("url","Title#3","Description#3")
    )
        .map {it.toEntity(dao.insert(it))}

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
        val toUpdate = testForms[2].copy(imageUrl = "https://github.com")
        dao.update(toUpdate)
        assertEquals(toUpdate,dao.selectOne(toUpdate.id))
    }

    @Test
    fun insert_adds_a_row()
    {
        val toInsert = FormValues("url","a different title","update")
        val insertedRowId = dao.insert(toInsert)
        val insertedRow = toInsert.toEntity(insertedRowId)
        assertEquals(insertedRow,dao.selectOne(insertedRowId))
        assertEquals(testForms+insertedRow,dao.selectAll())
    }
}
