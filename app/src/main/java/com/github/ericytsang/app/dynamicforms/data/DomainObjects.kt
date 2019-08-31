package com.github.ericytsang.app.dynamicforms.data

import android.util.Patterns
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import java.io.File

// domain objects
data class LocalFile(val filePath:String)
{
    init
    {
        require(File(filePath).exists()) {"file does not exist: $filePath"}
    }
}

data class Url(val url:String)
{
    init
    {
        require(Patterns.WEB_URL.matcher(url).matches()) {"malformed url: $url"}
    }
}



// room database
@Database(
        entities = [
            Form::class,
            FormField.TextFormField::class,
            FormField.DateFormField::class],
        version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase:RoomDatabase()
{
    abstract fun formDao():FormDao
}

// room DAOs
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

// converters
class Converters
{
    @TypeConverter
    fun stringToUrl(value:String):Url = Url(value)
    @TypeConverter
    fun urlToString(value:Url):String = value.url

    @TypeConverter
    fun stringToLocalFile(value:String):LocalFile = LocalFile(value)
    @TypeConverter
    fun localFileToString(value:LocalFile):String = value.filePath
}

// room entities
private interface IFormId
{
    val id:Long
}

private interface IFormValues
{
    /** file path to the display image downloaded from [imageUrl] */
    val imageFilePath:LocalFile

    /** url to an image online to be downloaded */
    val imageUrl:Url
}

@Entity
data class Form(
        @PrimaryKey(autoGenerate = true)
        override val id:Long,
        override val imageFilePath:LocalFile,
        override val imageUrl:Url)
    :IFormId,IFormValues
{
    val pk get() = FormPk(id)
    val values get() = FormValues(
            imageFilePath,
            imageUrl)
}

data class FormPk(
        override val id:Long)
    :IFormId

data class FormValues(
        override val imageFilePath:LocalFile,
        override val imageUrl:Url)
    :IFormValues

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
