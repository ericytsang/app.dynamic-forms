package com.github.ericytsang.app.dynamicforms.utils

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SingletonFactory<Params:Any,Singleton:Any>(
    private val factory:(Params)->Singleton
)
{
    private val lock = ReentrantLock()
    private var instance:Singleton? = null

    fun getInstance(params:Params):Singleton
    {
        return instance ?: lock.withLock()
        {
            instance ?: factory(params).also()
            {
                instance = it
            }
        }
    }
}
