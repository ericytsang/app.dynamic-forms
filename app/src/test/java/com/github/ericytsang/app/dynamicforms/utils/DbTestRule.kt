package com.github.ericytsang.app.dynamicforms.utils

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class DbTestRule<DB:RoomDatabase>(context:Context,databaseClass:Class<DB>):
    TestWatcher()
{
    val database = Room.inMemoryDatabaseBuilder(
        context,
        databaseClass
    ).allowMainThreadQueries().build()

    override fun finished(description:Description?)
    {
        database.close()
    }
}
