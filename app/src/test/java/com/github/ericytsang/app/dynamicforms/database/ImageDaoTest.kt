package com.github.ericytsang.app.dynamicforms.database

import androidx.test.platform.app.InstrumentationRegistry
import com.github.ericytsang.app.dynamicforms.database.ImageEntity.*
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
        ImageEntity(Pk("url#1"),Values("filePath#1")),
        ImageEntity(Pk("url#2"),Values("filePath#2")),
        ImageEntity(Pk("url#3"),Values("filePath#3"))
    )

    init
    {
        testImages.forEach {dao.insert(it)}
    }

    @Test
    fun selectOne_returns_one_rows()
    {
        val toSelect = testImages[0]
        assertEquals(toSelect,dao.selectOne(toSelect.pk))
    }

    @Test
    fun delete_deletes_a_row()
    {
        val toDelete = testImages[1]
        dao.delete(toDelete.pk)
        assertNull(dao.selectOne(toDelete.pk))
    }

    @Test
    fun insert_adds_a_row()
    {
        val toInsert = ImageEntity(Pk("url#4"),Values("different"))
        dao.insert(toInsert)
        assertEquals(toInsert,dao.selectOne(toInsert.pk))
        assertEquals(testImages+toInsert,dao.selectAll())
    }

    @Test
    fun insert_replaces_a_row()
    {
        val toInsert = ImageEntity(Pk("url#3"),Values("different"))
        dao.insert(toInsert)
        assertEquals(toInsert,dao.selectOne(toInsert.pk))
        assertEquals(
            testImages.filter {it.pk != toInsert.pk}+toInsert,
            dao.selectAll()
        )
    }
}
