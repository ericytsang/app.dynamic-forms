package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query


@Dao
abstract class ImageDao
{
    @Insert(entity = Image::class,onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(values:Image)

    @Query("SELECT * FROM Image")
    abstract fun selectAll():List<Image>

    @Query("SELECT * FROM Image WHERE url = :url")
    abstract fun selectOne(url:String):Image?

    @Delete(entity = Image::class)
    abstract fun delete(pk:ImagePk)
}


private interface IImagePk
{
    /** todo: @see TODO */
    val url:String
}

private interface IImageValues
{
    /** todo: @see TODO */
    // todo: maybe filepath should be:
    //  * the status of fetchig the image,
    //  * or the id of the task that was tasked to fetch the image, .... and is the path to the image if it is already done...
    val filePath:String
}


data class ImagePk(
    override val url:String
):IImagePk

data class ImageValues(
    override val filePath:String
):IImageValues


val Image.pk get() = ImagePk(url)

fun ImageValues.toEntity(url:String) = Image(
    url,
    filePath
)


@Entity
data class Image(
    @PrimaryKey(autoGenerate = false)
    override val url:String,
    override val filePath:String
):
    IImagePk,
    IImageValues
