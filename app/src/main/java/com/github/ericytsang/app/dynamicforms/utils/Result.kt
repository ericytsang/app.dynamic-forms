package com.github.ericytsang.app.dynamicforms.utils

sealed class Result<S,F>
{
    data class Success<S,F>(val success:S):
        Result<S,F>()
    {
        override fun <X> mapSuccess(mapper:(S)->X) =
            Success<X,F>(mapper(success))
        override fun <X> mapFailure(mapper:(F)->X) =
            Success<S,X>(success)
    }

    data class Failure<S,F>(val failure:F):
        Result<S,F>()
    {
        override fun <X> mapSuccess(mapper:(S)->X) =
            Failure<X,F>(failure)
        override fun <X> mapFailure(mapper:(F)->X) =
            Failure<S,X>(mapper(failure))
    }

    abstract fun <X> mapSuccess(mapper:(S)->X):Result<X,F>
    abstract fun <X> mapFailure(mapper:(F)->X):Result<S,X>
}