package com.github.ericytsang.app.dynamicforms.database

import androidx.test.platform.app.InstrumentationRegistry
import com.github.ericytsang.app.dynamicforms.utils.TestDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FormDaoTest
{
    @get:Rule
    val database = TestDatabase(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java)

    private val dao = database.database.formDao()

    private val testForms = listOf(
        FormValues("/file/path","https://headcheckhealth.com"),
        FormValues("/file/path","https://headcheckhealth.com"),
        FormValues("/file/path","https://headcheckhealth.com"))
        .map {it.toEntity(database.database.formDao().insert(it))}

    @Test
    fun select_all_returns_all_rows()
    {
        assertEquals(dao.selectAll(),testForms)
    }

    @Test
    fun delete_deletes_a_row()
    {
        dao.delete(testForms[1].pk)
        assertEquals(dao.selectAll().map {it.pk},listOf(testForms[0].pk,testForms[2].pk))
    }

    @Test
    fun update_updates_a_row()
    {
        val toUpdate = testForms[2].copy(imageUrl = "https://github.com")
        dao.update(toUpdate)
        assertEquals(dao.selectOne(toUpdate.id),toUpdate)
    }

    @Test
    fun insert_adds_a_row()
    {
        val toInsert = FormValues("/","https://github.com")
        val insertedRowId = dao.insert(toInsert)
        val insertedRow = toInsert.toEntity(insertedRowId)
        assertEquals(dao.selectOne(insertedRowId),insertedRow)
        assertEquals(dao.selectAll(),testForms+insertedRow)
    }

    private val Form.pk get() = FormPk(id)
    private fun FormValues.toEntity(id:Long) = Form(id,imageFilePath,imageUrl)
}
