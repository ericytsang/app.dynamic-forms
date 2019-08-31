package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Form::class,
        FormField.TextFormField::class,
        FormField.DateFormField::class],
    version = 1)
abstract class AppDatabase:RoomDatabase()
{
    abstract fun formDao():FormDao
}
