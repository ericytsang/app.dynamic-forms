package com.github.ericytsang.app.dynamicforms.repository

import android.util.Patterns

data class Url(val url:String)
{
    init
    {
        require(Patterns.WEB_URL.matcher(url).matches()) {"malformed url: $url"}
    }
}