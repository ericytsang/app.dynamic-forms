package com.github.ericytsang.app.dynamicforms.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.ericytsang.app.dynamicforms.repository.FormField


@Entity(
    foreignKeys = [
        ForeignKey(
            entity = FormEntity::class,
            parentColumns = ["id"],
            childColumns = ["formId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "formId",
                "positionInForm"
            ],
            unique = true
        )
    ]
)
data class FormFieldEntity(
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

        /** @see FormField.Values.formId */
        val formId:Long,

        /** @see FormField.Values.positionInForm */
        val positionInForm:Int,

        /** @see FormField.Values.isRequired */
        val isRequired:Boolean
    )
}

// todo: maybe we can get rid of FormFieldEntity, and replace it with the sealed class below
sealed class FormFieldEntitySubclass
{
    abstract val pk:FormFieldEntity.Pk
}

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = FormFieldEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class TextFormFieldEntity(
    @PrimaryKey(autoGenerate = false)
    @Embedded
    override val pk:FormFieldEntity.Pk,
    @Embedded
    val values:Values
):
    FormFieldEntitySubclass()
{
    data class Values(

        /** @see FormField.TextFormField.Values.userInput */
        val value:String
    )
}


@Entity(
    foreignKeys = [
        ForeignKey(
            entity = FormFieldEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class DateFormFieldEntity(
    @PrimaryKey(autoGenerate = false)
    @Embedded
    override val pk:FormFieldEntity.Pk,
    @Embedded
    val values:Values

):
    FormFieldEntitySubclass()
{
    data class Values(

        /** @see FormField.DateFormField.Values.userInput */
        val value:Long
    )
}