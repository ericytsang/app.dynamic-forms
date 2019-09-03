package com.github.ericytsang.app.dynamicforms.domainobjects

import android.content.Context
import com.github.ericytsang.app.dynamicforms.R
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

sealed class FormFieldReadData
{
    companion object
    {
        fun fromModel(model:FormField.Values):FormFieldReadData
        {
            return when (model)
            {
                is FormField.Values.Text -> Text(
                    model.positionInForm,
                    model.label,
                    model.isRequired,
                    model.userInput
                )
                is FormField.Values.Date -> Date(
                    model.positionInForm,
                    model.label,
                    model.isRequired,
                    model.userInput
                )
            }
        }

        private const val JSON_KEY__POSITION_IN_FORM = "positionInForm"
        private const val JSON_KEY__LABEL = "label"
        private const val JSON_KEY__IS_REQUIRED = "isRequired"
        private const val JSON_KEY__TYPE = "type"
        private const val JSON_KEY__VALUE = "value"

        fun fromJson(json:JSONObject):FormFieldReadData
        {
            return when(JsonTypeValue.valueOf(json.getString(
                JSON_KEY__TYPE
            ).toUpperCase(Locale.US)))
            {
                JsonTypeValue.TEXT -> Text(
                    json.getInt(JSON_KEY__POSITION_IN_FORM),
                    json.getString(JSON_KEY__LABEL),
                    json.getBoolean(JSON_KEY__IS_REQUIRED),
                    json.getString(JSON_KEY__VALUE)
                )
                JsonTypeValue.DATE -> Date(
                    json.getInt(JSON_KEY__POSITION_IN_FORM),
                    json.getString(JSON_KEY__LABEL),
                    json.getBoolean(JSON_KEY__IS_REQUIRED),
                    Calendar.getInstance().apply {
                        timeInMillis =
                            json.getLong(JSON_KEY__VALUE)
                    })
            }
        }
    }

    fun toModel(formPk:FormEntity.Pk):FormField.Values
    {
        return when (this)
        {
            is Text -> FormField.Values.Text(
                formPk,
                positionInForm,
                formFieldLabel,
                formFieldIsRequired,
                formFieldValue
            )
            is Date -> FormField.Values.Date(
                formPk,
                positionInForm,
                formFieldLabel,
                formFieldIsRequired,
                formFieldValue
            )
        }
    }

    /**
     * in additoin to testing, having this function also make sure that there's a corresponding
     * [JsonTypeValue] declared for each [FormFieldReadData] subclass.
     */
    fun toJsonTypeValue():JsonTypeValue
    {
        return when(this)
        {
            is Text -> JsonTypeValue.TEXT
            is Date -> JsonTypeValue.DATE
        }
    }

    enum class JsonTypeValue
    {
        TEXT,
        DATE,
        ;
    }

    /**
     * returns the user's input as a string to show to user.
     */
    fun userInputAsString(context:Context):String
    {
        return when (this)
        {
            is Text -> formFieldValue
            is Date -> formFieldValue?.toString()
                ?: context.getString(R.string.form_field_read_data__no_date_set)
        }
    }

    /**
     * returns a string if invalid; null otherwise.
     */
    val isValid:Boolean get()
    {
        return when(this)
        {
            is Text -> FormField.Values.Text.isValid(formFieldIsRequired,formFieldValue)
            is Date -> FormField.Values.Date.isValid(formFieldIsRequired,formFieldValue)
        }
    }

    abstract val positionInForm:Int
    abstract val formFieldLabel:String
    abstract val formFieldIsRequired:Boolean
    abstract override fun hashCode():Int
    abstract override fun equals(other:Any?):Boolean

    data class Text(
        override val positionInForm:Int,
        override val formFieldLabel:String,
        override val formFieldIsRequired:Boolean,
        val formFieldValue:String
    ):FormFieldReadData()

    data class Date(
        override val positionInForm:Int,
        override val formFieldLabel:String,
        override val formFieldIsRequired:Boolean,
        val formFieldValue:Calendar?
    ):FormFieldReadData()
}
