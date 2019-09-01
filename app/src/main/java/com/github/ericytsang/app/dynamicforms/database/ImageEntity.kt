package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey


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
