package com.github.ericytsang.app.dynamicforms.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.ericytsang.app.dynamicforms.utils.DbTestRule
import com.github.ericytsang.app.dynamicforms.utils.awaitValue
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MiscDatabaseTest
{
    @get:Rule
    val dbTestRule = DbTestRule(
        InstrumentationRegistry.getInstrumentation().targetContext,
        AppDatabase::class.java
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dao = dbTestRule.database.formDao()

    private val testForms = listOf(
        FormEntity.Values("url","Title#1","Description#1"),
        FormEntity.Values("url","Title#2","Description#2"),
        FormEntity.Values("url","Title#3","Description#3")
    )
        .map {FormEntity(dao.insert(it),it)}

    @Test
    fun transaction_can_roll_back_when_exception()
    {
        class AbortTransactionEx:Throwable("abort transaction")
        try
        {
            dbTestRule.database.runInTransaction()
            {
                dao.delete(testForms[1].pk)
                throw AbortTransactionEx()
            }
        }
        catch (ex:AbortTransactionEx)
        {
            // ignore
        }

        Assert.assertEquals(testForms,dao.selectAll().awaitValue())
    }
}
