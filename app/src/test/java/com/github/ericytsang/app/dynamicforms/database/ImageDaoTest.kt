package com.github.ericytsang.app.dynamicforms.database

import androidx.test.platform.app.InstrumentationRegistry
import com.github.ericytsang.app.dynamicforms.utils.DbTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImageDaoTest
{
    @get:Rule
    val dbTestRule = DbTestRule(
        InstrumentationRegistry.getInstrumentation().targetContext,
        AppDatabase::class.java
    )

    private val dao = dbTestRule.database.imageDao()

    private val testImages = listOf(
        Image("url#1","filePath#1"),
        Image("url#2","filePath#2"),
        Image("url#3","filePath#3")
    )

    init
    {
        testImages.forEach {dao.insert(it)}
    }

    @Test
    fun selectOne_returns_one_rows()
    {
        val toSelect = testImages[0]
        assertEquals(toSelect,dao.selectOne(toSelect.url))
    }

    @Test
    fun delete_deletes_a_row()
    {
        val toDelete = testImages[1]
        dao.delete(toDelete.pk)
        assertNull(dao.selectOne(toDelete.url))
    }

    @Test
    fun insert_adds_a_row()
    {
        val toInsert = Image("url#4","different")
        dao.insert(toInsert)
        assertEquals(toInsert,dao.selectOne(toInsert.url))
        assertEquals(testImages+toInsert,dao.selectAll())
    }

    @Test
    fun insert_replaces_a_row()
    {
        val toInsert = Image("url#3","different")
        dao.insert(toInsert)
        assertEquals(toInsert,dao.selectOne(toInsert.url))
        assertEquals(
            testImages.filter {it.url != toInsert.url}+toInsert,
            dao.selectAll()
        )
    }
}
