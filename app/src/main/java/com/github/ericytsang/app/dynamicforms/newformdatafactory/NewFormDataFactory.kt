package com.github.ericytsang.app.dynamicforms.newformdatafactory

import com.github.ericytsang.app.dynamicforms.utils.Result

interface NewFormDataFactory
{
    fun make():Result<NewFormData,String>
}
