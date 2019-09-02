package com.github.ericytsang.app.dynamicforms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.ericytsang.app.dynamicforms.databinding.LayoutListWithFabBinding
import com.github.ericytsang.app.dynamicforms.databinding.ListItemFormBinding
import com.github.ericytsang.app.dynamicforms.domainobjects.Form


class FormListFragment:Fragment()
{
    override fun onCreateView(
        inflater:LayoutInflater,
        container:ViewGroup?,
        savedInstanceState:Bundle?
    ):View?
    {
        val context = context!!
        val viewBinding = LayoutListWithFabBinding.inflate(inflater,container,false)
        val viewModel = InjectorUtils.getMainActivityViewModel(activity!!)

        val listener = object:FormViewHolder.Listener
        {
            override fun onClick(item:Form)
            {
                viewModel.selectOne(item.pk)
            }
        }
        viewBinding.recyclerView.adapter = FormAdapter(listener).apply()
        {
            viewModel.formList.observe(viewLifecycleOwner)
            {
                submitList(it)
            }
        }
        viewBinding.fab.setImageDrawable(context.getDrawableCompat(R.drawable.ic_add_black_24dp))
        viewBinding.fab.setOnClickListener()
        {
            view:View ->
            viewModel.openNewFormForEditing(view.context)
        }

        return viewBinding.root
    }
}


private class FormAdapter(
    private val listener:FormViewHolder.Listener
):
    ListAdapter<Form,FormViewHolder>(diffCallback)
{
    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):FormViewHolder
    {
        return FormViewHolder(
            ListItemFormBinding.inflate(parent.context.layoutInflater,parent,false)
        )
    }

    override fun onBindViewHolder(holder:FormViewHolder,position:Int)
    {
        holder.bind(listener,getItem(position) ?: return)
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


// todo
private data class FormViewHolderModel(
    val form:Form,
    val isSelected:Boolean
)


private class FormViewHolder(
    private val viewBinding:ListItemFormBinding
):RecyclerView.ViewHolder(viewBinding.root)
{
    fun bind(listener:Listener,item:Form)
    {
        viewBinding.apply()
        {
            root.setOnClickListener {listener.onClick(item)}
            title.text = item.values.title
            description.text = item.values.description
            item.values.imageUrl.url // todo: display it?

        }
    }

    interface Listener
    {
        fun onClick(item:Form)
    }
}