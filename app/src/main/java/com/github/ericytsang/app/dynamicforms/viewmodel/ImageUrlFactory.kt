package com.github.ericytsang.app.dynamicforms.viewmodel

import com.github.ericytsang.app.dynamicforms.domainobjects.Url

interface ImageUrlFactory
{
    fun make():Url
}