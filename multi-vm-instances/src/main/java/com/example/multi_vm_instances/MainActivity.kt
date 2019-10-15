package com.example.multi_vm_instances

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.multi_vm_instances.databinding.ActivityChildBinding
import com.example.multi_vm_instances.databinding.ActivityMainBinding
import java.io.Serializable
import kotlin.random.Random

class MainActivity:AppCompatActivity()
{

    override fun onCreate(savedInstanceState:Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }
}

class ChildActivity:AppCompatActivity()
{
    companion object
    {
        fun start(context:Context,params:Params)
        {
            context.startActivity(
                Intent(context,ChildActivity::class.java).apply()
                {
                    putExtra("asdf",params)
                })
        }
    }

    data class Params(val textToDisplay:String):Serializable

    private val viewModel:MyViewModel by viewModels<MyViewModel> {ViewModelFactory()}

    override fun onCreate(savedInstanceState:Bundle?)
    {
        super.onCreate(savedInstanceState)
        val binding = setContentView<ActivityChildBinding>(this, R.layout.activity_child)
        val params = intent.getSerializableExtra("asdf") as Params
        binding.name = params.textToDisplay + " " + viewModel.id
    }
}

@BindingAdapter("app:startChildActivity")
fun startChildActivity(button:AppCompatButton,text:String)
{
    button.setOnClickListener()
    {
        ChildActivity.start(button.context,ChildActivity.Params(text))
    }
}

class MyViewModel:ViewModel()
{
    val id = Random.nextInt()
        get()
        {
            Log.d(this::class.simpleName, "getId() = $field")
            return field
        }
}

class ViewModelFactory: ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>):T
    {
        return MyViewModel() as T
    }
}
