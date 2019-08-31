package com.github.ericytsang.app.dynamicforms.data

import android.util.Patterns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.File

// domain objects
data class LocalFile(val filePath: File)
{
    init
    {
        require(filePath.exists()) {"file does not exist: $filePath"}
    }
}

data class Url(val url:String)
{
    init
    {
        require(Patterns.WEB_URL.matcher(url).matches()) {"malformed url: $url"}
    }
}

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
