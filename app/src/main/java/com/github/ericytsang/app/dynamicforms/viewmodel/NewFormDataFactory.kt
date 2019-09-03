package com.github.ericytsang.app.dynamicforms.viewmodel

import com.github.ericytsang.app.dynamicforms.utils.Result

interface NewFormDataFactory
{
    fun make():Result<NewFormData,String>
}
