package com.github.ericytsang.app.dynamicforms.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.ericytsang.app.dynamicforms.utils.SingletonFactory

@Database(
    entities = [
        ImageEntity::class,
        FormEntity::class,
        FormField.TextFormField::class,
        FormField.DateFormField::class],
    version = 1
)
abstract class AppDatabase:RoomDatabase()
{
    companion object
    {
        val factory = SingletonFactory()
        {context:Context ->
            Room
                .databaseBuilder(context.applicationContext,AppDatabase::class.java,"database.db")
                .build()
        }
    }

    abstract fun formDao():FormDao
    abstract fun imageDao():ImageDao
}
