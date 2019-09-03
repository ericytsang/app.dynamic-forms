package com.github.ericytsang.app.dynamicforms.newformdatafactory

import com.github.ericytsang.app.dynamicforms.domainobjects.Url

class LoremPicsum:ImageUrlFactory
{
    override fun make():Url
    {
        return Url("https://picsum.photos/id/${(0..1084).random()}/200/200")
    }
}