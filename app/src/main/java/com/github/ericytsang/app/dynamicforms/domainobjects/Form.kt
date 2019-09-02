package com.github.ericytsang.app.dynamicforms.domainobjects

import com.github.ericytsang.app.dynamicforms.database.FormEntity

data class Form(
    val pk:FormEntity.Pk,
    val values:Values
)
{
    companion object
    {
        fun fromEntity(entity:FormEntity):Form
        {
            return Form(
                entity.pk,
                Values(
                    Url(
                        entity.values.imageUrl
                    ),
                    entity.values.title,
                    entity.values.description
                )
            )
        }
    }

    fun toEntity() = FormEntity(pk,values.toEntity())

    data class Values(

        /** image associated with this form */
        val imageUrl:Url,

        /** primary text to show as the title of list items in list views */
        val title:String,

        /** secondary text to show as the subtitle of list items in list views. */
        val description:String
    )
    {
        fun toEntity() = FormEntity.Values(imageUrl.url,title,description)
    }
}