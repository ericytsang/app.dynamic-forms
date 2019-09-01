package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Database
import androidx.room.RoomDatabase

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
    abstract fun formDao():FormDao
    abstract fun imageDao():ImageDao
}
