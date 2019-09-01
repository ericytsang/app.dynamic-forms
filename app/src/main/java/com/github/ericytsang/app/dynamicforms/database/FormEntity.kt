package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update


@Dao
abstract class FormDao
{
    @Insert(entity = FormEntity::class)
    protected abstract fun _insert(values:FormEntity.Values):Long

    fun insert(values:FormEntity.Values) = FormEntity.Pk(_insert(values))

    @Query("SELECT * FROM FormEntity")
    abstract fun selectAll():List<FormEntity>

    @Query("SELECT * FROM FormEntity WHERE id = :id")
    protected abstract fun _selectOne(id:Long):FormEntity?

    fun selectOne(pk:FormEntity.Pk) = _selectOne(pk.id)

    @Update(entity = FormEntity::class,onConflict = OnConflictStrategy.ABORT)
    abstract fun update(form:FormEntity)

    @Delete(entity = FormEntity::class)
    abstract fun delete(pk:FormEntity.Pk)
}


@Entity
data class FormEntity(
    @PrimaryKey(autoGenerate = true)
    @Embedded
    val pk:Pk,
    @Embedded
    val values:Values
)
{
    data class Pk(
        val id:Long
    )

    data class Values(

        /** @see com.github.ericytsang.app.dynamicforms.repository.FormValues.imageUrl */
        val imageUrl:String,

        /** @see com.github.ericytsang.app.dynamicforms.repository.FormValues.title */
        val title:String,

        /** @see com.github.ericytsang.app.dynamicforms.repository.FormValues.description */
        val description:String
    )
}