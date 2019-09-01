package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
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


@Entity
data class ImageEntity(
    @PrimaryKey(autoGenerate = false)
    @Embedded
    val pk:Pk,
    @Embedded
    val values:Values
)
{
    data class Pk(

        /** todo: @see TODO */
        val url:String
    )

    data class Values(

        /** todo: @see TODO */
        // todo: maybe filepath should be:
        //  * the status of fetchig the image,
        //  * or the id of the task that was tasked to fetch the image, .... and is the path to the image if it is already done...
        val filePath:String
    )
}
