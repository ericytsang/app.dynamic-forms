package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey


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
    )

    data class Values(

        /** @see com.github.ericytsang.app.dynamicforms.repository.Form.Values.imageUrl */
        val imageUrl:String,

        /** @see com.github.ericytsang.app.dynamicforms.repository.Form.Values.title */
        val title:String,

        /** @see com.github.ericytsang.app.dynamicforms.repository.Form.Values.description */
        val description:String
    )
}
