package com.github.ericytsang.app.dynamicforms.repository

import android.util.Patterns
import java.io.File

data class LocalFile(val filePath:String)
{
    init
    {
        require(File(filePath).exists()) {"file does not exist: $filePath"}
    }
}

data class Url(val url:String)
{
    init
    {
        require(Patterns.WEB_URL.matcher(url).matches()) {"malformed url: $url"}
    }
}


// room entities
private interface IFormId
{
    val id:Long
}

private interface IFormValues
{
    /** file path to the display image downloaded from [imageUrl] */
    val imageFilePath:LocalFile

    /** url to an image online to be downloaded */
    val imageUrl:Url
}

data class FormPk(
    val id:Long)

data class FormValues(
    val imageFilePath:LocalFile,
    val imageUrl:Url)
