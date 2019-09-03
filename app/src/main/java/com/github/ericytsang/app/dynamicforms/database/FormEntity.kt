package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.ericytsang.app.dynamicforms.domainobjects.Form
import java.io.Serializable


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
    ):Serializable

    data class Values(

        /** @see Form.Values.imageUrl */
        val imageUrl:String,

        /** @see Form.Values.title */
        val title:String,

        /** @see Form.Values.description */
        val description:String
    )
}
