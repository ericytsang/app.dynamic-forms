package com.github.ericytsang.app.dynamicforms.repository

data class Image(
    val pk:Pk,
    val values:Values
)
{
    data class Pk(
        val url:Url
    )

    sealed class Values
    {
        class DownloadInProgress()
        class Downloaded(val filePath:LocalFile)
    }
}