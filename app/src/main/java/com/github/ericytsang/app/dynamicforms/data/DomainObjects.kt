package com.github.ericytsang.app.dynamicforms.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class Form(
        @PrimaryKey(autoGenerate = true)
        val id:Long,
        val imageFilePath:String,
        val imageUrl:String)

sealed class FormField
{
    abstract val id:Long
    abstract val formId:Long
    abstract val isRequired:Boolean

    @Entity
    data class TextFormField(
            @PrimaryKey(autoGenerate = true)
            override val id:Long,
            @ForeignKey(entity = Form::class)
            override val formId:Long,
            override val isRequired:Boolean,
            val value:String)
        :FormField()

    @Entity
    data class DateFormField(
            @PrimaryKey(autoGenerate = true)
            override val id:Long,
            @ForeignKey(entity = Form::class)
            override val formId:Long,
            override val isRequired:Boolean,
            val value:Long)
        :FormField()
}
