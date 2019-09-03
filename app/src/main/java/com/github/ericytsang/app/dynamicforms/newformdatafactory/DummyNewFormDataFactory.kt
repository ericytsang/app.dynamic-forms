package com.github.ericytsang.app.dynamicforms.newformdatafactory

import com.github.ericytsang.app.dynamicforms.domainobjects.FormFieldReadData
import com.github.ericytsang.app.dynamicforms.utils.Result

class DummyNewFormDataFactory:
    NewFormDataFactory
{
    private val loremPicsum =
        LoremPicsum()
    override fun make() =
        Result.Success<NewFormData,String>(
            NewFormData(
                loremPicsum.make(),
                listOf(
                    FormFieldReadData.Text(
                        0,
                        "Hello World!",
                        false,
                        "initial value"
                    ),
                    FormFieldReadData.Text(
                        1,
                        "is it finally working?",
                        false,
                        ""
                    ),
                    FormFieldReadData.Text(
                        2,
                        "let's see...",
                        false,
                        "initial value"
                    )
                )
            )
        )
}