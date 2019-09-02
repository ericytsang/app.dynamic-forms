package com.github.ericytsang.app.dynamicforms.json

import com.github.ericytsang.app.dynamicforms.FormFieldViewHolderModel
import junit.framework.Assert.assertEquals
import org.json.JSONArray
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
class JsonFormFieldSerializationTest
{
    companion object
    {
        private const val JSON_FILE_PATH = "serialized_form.json"
    }

    @Test
    fun get_file_from_resources()
    {
        JsonFormFieldSerializationTest::class.java.classLoader!!
            .getResourceAsStream(JSON_FILE_PATH)!!
            .use {}
    }

    @Test
    fun parse_file_as_json()
    {
        JsonFormFieldSerializationTest::class.java.classLoader!!
            .getResourceAsStream(JSON_FILE_PATH)!!
            .use()
            {fis ->
                val os = ByteArrayOutputStream()
                fis.copyTo(os)
                JSONArray(String(os.toByteArray()))[6]
            }
    }

    @Test
    fun parse_json_to_FormField()
    {
        val formFields = JsonFormFieldSerializationTest::class.java.classLoader!!
            .getResourceAsStream(JSON_FILE_PATH)!!
            .use {fis -> JSONArray(String(fis.readBytes()))}
            .let {jsonArray -> (0 until jsonArray.length()).map {jsonArray.getJSONObject(it)}}
            .map {FormFieldViewHolderModel.fromJson(it)}
        assertEquals(20,formFields.size)
    }

    @Test
    fun deserialize_at_least_one_json_object_per_FormFieldType()
    {
        val formFields = JsonFormFieldSerializationTest::class.java.classLoader!!
            .getResourceAsStream(JSON_FILE_PATH)!!
            .use {fis -> JSONArray(String(fis.readBytes()))}
            .let {jsonArray -> (0 until jsonArray.length()).map {jsonArray.getJSONObject(it)}}
            .map {FormFieldViewHolderModel.fromJson(it)}
        val parsedFormFieldTypes = formFields.map {it.toJsonTypeValue()}.toSet()
        assertEquals(FormFieldViewHolderModel.Companion.JsonTypeValue.values().toSet(),parsedFormFieldTypes)
    }
}
