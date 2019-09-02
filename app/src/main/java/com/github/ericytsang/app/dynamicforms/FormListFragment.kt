package com.github.ericytsang.app.dynamicforms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.ericytsang.app.dynamicforms.database.AppDatabase
import com.github.ericytsang.app.dynamicforms.databinding.FragmentFormListBinding
import com.github.ericytsang.app.dynamicforms.databinding.ListItemFormBinding
import com.github.ericytsang.app.dynamicforms.domainobjects.Form
import com.github.ericytsang.app.dynamicforms.repository.FormRepo
import com.github.ericytsang.app.dynamicforms.viewmodel.MainActivityViewModel

object InjectorUtils
{

    private fun getAppDatabase(context:Context):AppDatabase
    {
        return AppDatabase.factory.getInstance(context.applicationContext)
    }

    private fun getFormRepo(context:Context):FormRepo
    {
        return FormRepo(getAppDatabase(context))
    }

    fun getMainActivityViewModel(context:Context):MainActivityViewModel
    {
        return MainActivityViewModel.factory.getInstance(getFormRepo(context))
    }
}


class FormListFragment:Fragment()
{
    override fun onCreateView(
        inflater:LayoutInflater,
        container:ViewGroup?,
        savedInstanceState:Bundle?
    ):View?
    {
        val viewBinding = FragmentFormListBinding.inflate(inflater,container,false)
        val viewModel = InjectorUtils.getMainActivityViewModel(activity!!)

        viewBinding.recyclerView.adapter = FormAdapter().apply()
        {
            viewModel.formListItems.observe(viewLifecycleOwner)
            {
                submitList(it)
            }
        }
        viewBinding.fab.setOnClickListener()
        {
            viewModel.addRandomForm()
        }

        return viewBinding.root
    }
}


private class FormAdapter:ListAdapter<Form,FormViewHolder>(diffCallback)
{
    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):FormViewHolder
    {
        return FormViewHolder(
            ListItemFormBinding.inflate(parent.context.layoutInflater,parent,false)
        )
    }

    override fun onBindViewHolder(holder:FormViewHolder,position:Int)
    {
        holder.bind(getItem(position) ?: return)
    }

    companion object
    {
        private val diffCallback = object:DiffUtil.ItemCallback<Form>()
        {
            override fun areItemsTheSame(oldItem:Form,newItem:Form):Boolean
            {
                return oldItem.pk == newItem.pk
            }

            override fun areContentsTheSame(oldItem:Form,newItem:Form):Boolean
            {
                return oldItem == newItem
            }
        }
    }
}

private class FormViewHolder(
    private val viewBinding:ListItemFormBinding
):RecyclerView.ViewHolder(viewBinding.root)
{
    fun bind(item:Form)
    {
        viewBinding.apply()
        {
            title.text = item.values.title
            description.text = item.values.description
            item.values.imageUrl.url
        }
    }
}