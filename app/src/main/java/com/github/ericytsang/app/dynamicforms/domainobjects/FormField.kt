package com.github.ericytsang.app.dynamicforms.domainobjects

import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.database.FormFieldEntity
import java.util.Calendar

sealed class FormField
{
    abstract val pk:FormFieldEntity.Pk
    abstract val values:Values

    interface Values
    {
        /** [FormEntity] that this [FormField] is a part of */
        val formId:FormEntity.Pk

        /** [FormField]s are sorted in ascending order of [positionInForm] when displayed in [FormFragment].todo: link the form fragment */
        val positionInForm:Int

        /** whether this field requires a non-empty value to be saved */
        val isRequired:Boolean
    }

    data class TextFormField(
        override val pk:FormFieldEntity.Pk,
        override val values:Values
    ):FormField()
    {
        data class Values(
            override val formId:FormEntity.Pk,
            override val positionInForm:Int,
            override val isRequired:Boolean,

            /** the text value that the user entered for this input field */
            val userInput:String
        ):FormField.Values
    }

    data class DateFormField(
        override val pk:FormFieldEntity.Pk,
        override val values:Values
    ):FormField()
    {
        data class Values(
            override val formId:FormEntity.Pk,
            override val positionInForm:Int,
            override val isRequired:Boolean,

            /** the date value that the user entered for this input field */
            val userInput:Calendar
        ):FormField.Values
    }
}
