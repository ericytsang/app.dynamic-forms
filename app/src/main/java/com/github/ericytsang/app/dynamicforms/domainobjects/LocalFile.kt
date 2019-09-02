package com.github.ericytsang.app.dynamicforms.domainobjects

import java.io.File

// TODO: this will ultimately be sed to produce an image from bytes
//  maybe we need to make the interface higher level... i.e. add a getInputStream function
//  maybe all it needs is a getInputStream function.. hmm

data class LocalFile(val filePath:String)
{
    init
    {
        require(File(filePath).exists()) {"file does not exist: $filePath"}
    }
}
