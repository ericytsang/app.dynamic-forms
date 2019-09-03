package com.github.ericytsang.app.dynamicforms.viewmodel

import com.github.ericytsang.app.dynamicforms.domainobjects.FormFieldReadData
import com.github.ericytsang.app.dynamicforms.domainobjects.Url

data class NewFormData(
    val imageUrl:Url,
    val formFields:List<FormFieldReadData>
)