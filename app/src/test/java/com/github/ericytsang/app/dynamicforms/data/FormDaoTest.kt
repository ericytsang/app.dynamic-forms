package com.github.ericytsang.app.dynamicforms.data

import androidx.test.platform.app.InstrumentationRegistry
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

    init
    {
        database.database.formDao().insert(
                FormValues(LocalFile("/"),Url("https://headcheckhealth.com")),
                FormValues(LocalFile("/"),Url("https://headcheckhealth.com")),
                FormValues(LocalFile("/"),Url("https://headcheckhealth.com")))
    }

    @Test
    fun select_all_returns_all_rows()
    {
        assertEquals(
            dao.selectAll(),
            listOf(
                Form(1,LocalFile("/"),Url("https://headcheckhealth.com")),
                Form(2,LocalFile("/"),Url("https://headcheckhealth.com")),
                Form(3,LocalFile("/"),Url("https://headcheckhealth.com"))))
    }
}
