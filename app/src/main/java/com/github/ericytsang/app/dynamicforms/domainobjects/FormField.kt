package com.github.ericytsang.app.dynamicforms.domainobjects

import com.github.ericytsang.app.dynamicforms.FormDetailFragment
import com.github.ericytsang.app.dynamicforms.database.DateFormFieldEntity
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.database.FormFieldEntity
import com.github.ericytsang.app.dynamicforms.database.FormFieldEntitySubclass
import com.github.ericytsang.app.dynamicforms.database.TextFormFieldEntity
import java.util.Calendar

sealed class FormField
{
    companion object
    {
        fun fromEntities(
            parentFormFieldEntityValues:FormFieldEntity.Values,
            formFieldEntitySubclass:FormFieldEntitySubclass
        ):FormField
        {
            return when (formFieldEntitySubclass)
            {
                is TextFormFieldEntity -> TextFormField(
                    formFieldEntitySubclass.pk,
                    Values.Text(
                        FormEntity.Pk(parentFormFieldEntityValues.formId),
                        parentFormFieldEntityValues.positionInForm,
                        parentFormFieldEntityValues.label,
                        parentFormFieldEntityValues.isRequired,
                        formFieldEntitySubclass.values.value
                    )
                )
                is DateFormFieldEntity -> DateFormField(
                    formFieldEntitySubclass.pk,
                    Values.Date(
                        FormEntity.Pk(parentFormFieldEntityValues.formId),
                        parentFormFieldEntityValues.positionInForm,
                        parentFormFieldEntityValues.label,
                        parentFormFieldEntityValues.isRequired,
                        formFieldEntitySubclass.values.value?.let {
                            Calendar.getInstance().apply {timeInMillis = it}
                        }
                    )
                )
            }
        }
    }

    abstract val pk:FormFieldEntity.Pk
    abstract val values:Values

    fun toFormFieldEntity() = FormFieldEntity(pk,values.toFormFieldEntityValues())

    sealed class Values
    {
        /** [FormEntity] that this [FormField] is a part of */
        abstract val formId:FormEntity.Pk

        /** [FormField]s are sorted in ascending order of [positionInForm] when displayed in [FormDetailFragment] */
        abstract val positionInForm:Int

        /** the human-readable name for this form field e.g. "phone number", "email"  */
        abstract val label:String

        /** whether this field requires a non-empty value to be saved */
        abstract val isRequired:Boolean

        fun toFormFieldEntityValues() =
            FormFieldEntity.Values(formId.id,positionInForm,label,isRequired)

        data class Text(
            override val formId:FormEntity.Pk,
            override val positionInForm:Int,
            override val label:String,
            override val isRequired:Boolean,

            /** the text value that the user entered for this input field */
            val userInput:String
        ):
            FormField.Values()
        {
            // todo: i wonder if this is possible.....
            //  it might pose a problem when we want to create VMs for a form....
            //  but in that particular case, maybe we should create a different kind of class
            init
            {
                require(!(isRequired && userInput.isBlank()))
            }

            fun toTextFormFieldEntityValues() = TextFormFieldEntity.Values(userInput)
        }

        data class Date(
            override val formId:FormEntity.Pk,
            override val positionInForm:Int,
            override val label:String,
            override val isRequired:Boolean,

            /** the date value that the user entered for this input field */
            val userInput:Calendar?
        ):
            FormField.Values()
        {
            // todo: i wonder if this is possible.....
            //  it might pose a problem when we want to create VMs for a form....
            //  but in that particular case, maybe we should create a different kind of class
            init
            {
                require(!(isRequired && userInput != null))
            }

            fun toDateFormFieldEntityValues() = DateFormFieldEntity.Values(userInput?.timeInMillis)
        }
    }

    data class TextFormField(
        override val pk:FormFieldEntity.Pk,
        override val values:Values.Text
    ):FormField()
    {
        fun toTextFormFieldEntity() = TextFormFieldEntity(
            pk,
            values.toTextFormFieldEntityValues()
        )
    }

    data class DateFormField(
        override val pk:FormFieldEntity.Pk,
        override val values:Values.Date
    ):FormField()
    {
        fun toDateFormFieldEntity() = DateFormFieldEntity(
            pk,
            values.toDateFormFieldEntityValues()
        )
    }
}
