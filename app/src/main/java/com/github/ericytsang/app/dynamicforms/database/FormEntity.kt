package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update



@Dao
abstract class FormDao
{
    @Insert(entity = Form::class)
    abstract fun insert(values:FormValues):Long

    @Insert(entity = Form::class)
    abstract fun insert(vararg values:FormValues)

    @Query("SELECT * FROM Form")
    abstract fun selectAll():List<Form>

    @Query("SELECT * FROM Form WHERE id = :id")
    abstract fun selectOne(id:Long):Form?

    @Update(entity = Form::class, onConflict = OnConflictStrategy.ABORT)
    abstract fun update(form:Form)

    @Delete(entity = Form::class)
    abstract fun delete(pk:FormPk)
}



private interface IFormId
{
    val id:Long
}

private interface IFormValues
{
    /** file path to the display image downloaded from [imageUrl] */
    val imageFilePath:String

    /** url to an image online to be downloaded */
    val imageUrl:String
}



data class FormPk(
    override val id:Long)
    :IFormId

data class FormValues(
    override val imageFilePath:String,
    override val imageUrl:String)
    :IFormValues



@Entity
data class Form(
        @PrimaryKey(autoGenerate = true)
        override val id:Long,
        override val imageFilePath:String,
        override val imageUrl:String)
    :IFormId,IFormValues
