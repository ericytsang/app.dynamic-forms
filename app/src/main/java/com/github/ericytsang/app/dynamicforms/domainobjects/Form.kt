package com.github.ericytsang.app.dynamicforms.domainobjects

import com.github.ericytsang.app.dynamicforms.database.FormEntity

data class Form(
    val pk:FormEntity.Pk,
    val values:Values
)
{
    data class Values(

        /** image associated with this form */
        val imageUrl:Url,

        /** primary text to show as the title of list items in list views */
        val title:String,

        /** secondary text to show as the subtitle of list items in list views. */
        val description:String
    )
}