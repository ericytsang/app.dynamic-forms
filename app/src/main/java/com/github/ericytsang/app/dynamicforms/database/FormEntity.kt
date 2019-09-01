package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
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

    @Query("SELECT * FROM Form")
    abstract fun selectAll():List<Form>

    @Query("SELECT * FROM Form WHERE id = :id")
    abstract fun selectOne(id:Long):Form?

    @Update(entity = Form::class,onConflict = OnConflictStrategy.ABORT)
    abstract fun update(form:Form)

    @Delete(entity = Form::class)
    abstract fun delete(pk:FormPk)
}


private interface IFormPk
{
    val id:Long
}

private interface IFormValues
{
    /** @see com.github.ericytsang.app.dynamicforms.repository.FormValues.imageUrl */
    val imageUrl:String

    /** @see com.github.ericytsang.app.dynamicforms.repository.FormValues.title */
    val title:String

    /** @see com.github.ericytsang.app.dynamicforms.repository.FormValues.description */
    val description:String
}


data class FormPk(
    override val id:Long
):IFormPk

data class FormValues(
    override val imageUrl:String,
    override val title:String,
    override val description:String
):IFormValues


val Form.pk get() = FormPk(id)

fun FormValues.toEntity(id:Long) = Form(
    id,
    imageUrl,
    title,
    description
)


@Entity
data class Form(
    @PrimaryKey(autoGenerate = true)
    override val id:Long,
    override val imageUrl:String,
    override val title:String,
    override val description:String
):
    IFormPk,
    IFormValues
