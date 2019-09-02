package com.github.ericytsang.app.dynamicforms.database

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
abstract class FormDao
{
    @Insert(entity = FormEntity::class)
    protected abstract fun _insert(values:FormEntity.Values):Long
    fun insert(values:FormEntity.Values) = FormEntity.Pk(_insert(values))

    // todo: paging
    @Query("SELECT * FROM FormEntity")
    abstract fun selectAll():LiveData<List<FormEntity>>

    @Query("SELECT * FROM FormEntity WHERE id = :id")
    protected abstract fun _selectOne(id:Long):FormEntity?
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun selectOne(pk:FormEntity.Pk) = _selectOne(pk.id)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Update(entity = FormEntity::class,onConflict = OnConflictStrategy.ABORT)
    abstract fun update(form:FormEntity)

    @Delete(entity = FormEntity::class)
    abstract fun delete(pk:FormEntity.Pk)
}
