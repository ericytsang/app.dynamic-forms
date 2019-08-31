package com.github.ericytsang.app.dynamicforms.data

import org.hamcrest.core.StringContains
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UrlTest
{
    @Test
    fun cannot_create_malformed_urls()
    {
        // should throw exception when url is instantiated with invalid data
        val exception = runCatching {
            Url("malformed url")
        }.exceptionOrNull()

        // then it should throw an exception
        assert(exception is IllegalArgumentException) {"exception was: ${exception?.printStackTrace()}"}
        assertThat(exception?.message, StringContains("malformed url: malformed url"))
    }

    @Test
    fun can_create_well_formed_urls()
    {
        // no exceptions when when a url is instantiated with valid data
        Url("https://headcheckhealth.com")
    }

    @Test
    fun get_url_returns_same_url()
    {
        val urlString = "https://headcheckhealth.com"

        val url = Url(urlString)

        assertEquals(urlString,url.url)
    }

    @Test
    fun same_url_instances_are_equal()
    {
        assertEquals(
                Url("https://headcheckhealth.com"),
                Url("https://headcheckhealth.com"))
    }

    @Test
    fun different_url_instances_are_not_equal()
    {
        assertNotEquals(
                Url("https://githum.com"),
                Url("https://headcheckhealth.com"))
    }
}
