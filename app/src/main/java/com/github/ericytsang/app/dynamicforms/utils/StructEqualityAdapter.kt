package com.github.ericytsang.app.dynamicforms.utils

data class StructEqualityAdapter<V>(val key:Any,val value:V)
{
    override fun hashCode():Int = key.hashCode()
    override fun equals(other:Any?):Boolean
    {
        return if (other is StructEqualityAdapter<*>)
        {
            key == other.key
        }
        else
        {
            key == other
        }
    }
}