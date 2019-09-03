package com.github.ericytsang.app.dynamicforms.utils

import java.io.Closeable

data class CloseAdapter<T>(val wrapped:T,val _close:T.()->Unit):Closeable
{
    override fun close()
    {
        with(wrapped)
        {
            _close()
        }
    }
}