package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class ImageDao
{
    @Insert(entity = ImageEntity::class,onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(values:ImageEntity)

    @Query("SELECT * FROM ImageEntity")
    abstract fun selectAll():List<ImageEntity>

    @Query("SELECT * FROM ImageEntity WHERE url = :url")
    protected abstract fun _selectOne(url:String):ImageEntity?
    fun selectOne(pk:ImageEntity.Pk) = _selectOne(pk.url)

    @Delete(entity = ImageEntity::class)
    abstract fun delete(pk:ImageEntity.Pk)
}
