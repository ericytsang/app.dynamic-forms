package com.github.ericytsang.app.dynamicforms.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

sealed class FormField
{
    abstract val id:Long
    abstract val formId:Long
    abstract val isRequired:Boolean

    @Entity(foreignKeys = [
        ForeignKey(
            entity = Form::class,
            parentColumns = ["id"],
            childColumns = ["formId"],
            onDelete = ForeignKey.CASCADE)])
    data class TextFormField(
        @PrimaryKey(autoGenerate = true)
        override val id:Long,
        @ColumnInfo(index = true)
        override val formId:Long,
        override val isRequired:Boolean,
        val value:String)
        :FormField()

    @Entity(foreignKeys = [
        ForeignKey(
            entity = Form::class,
            parentColumns = ["id"],
            childColumns = ["formId"],
            onDelete = ForeignKey.CASCADE)])
    data class DateFormField(
        @PrimaryKey(autoGenerate = true)
        override val id:Long,
        @ColumnInfo(index = true)
        override val formId:Long,
        override val isRequired:Boolean,
        val value:Long)
        :FormField()
}
