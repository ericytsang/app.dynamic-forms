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
            override fun onClick(item:FormViewHolderModel)
            {
                viewModel.selectOne(item.form.pk)
            }

            override fun onLongClick(item:FormViewHolderModel)
            {
                viewModel.delete(item.form.pk)
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
            viewModel.openNewFormForEditing()
        }

        return viewBinding.root
    }
}


private class FormAdapter(
    private val listener:FormViewHolder.Listener
):
    ListAdapter<FormViewHolderModel,FormViewHolder>(diffCallback)
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
        private val diffCallback = object:DiffUtil.ItemCallback<FormViewHolderModel>()
        {
            override fun areItemsTheSame(oldItem:FormViewHolderModel,newItem:FormViewHolderModel):Boolean
            {
                return oldItem.form.pk == newItem.form.pk
            }

            override fun areContentsTheSame(oldItem:FormViewHolderModel,newItem:FormViewHolderModel):Boolean
            {
                return oldItem == newItem
            }
        }
    }
}


data class FormViewHolderModel(
    val form:Form,
    val isSelected:Boolean
)


private class FormViewHolder(
    private val viewBinding:ListItemFormBinding
):RecyclerView.ViewHolder(viewBinding.root)
{
    fun bind(listener:Listener,item:FormViewHolderModel)
    {
        viewBinding.apply()
        {
            root.setOnClickListener {listener.onClick(item)}
            root.setOnLongClickListener()
            {
                listener.onLongClick(item)
                true
            }
            title.text = item.form.values.title
            description.text = item.form.values.description
            val backgroundColor = if (item.isSelected)
            {
                root.context.getColorCompat(android.R.color.holo_blue_bright)
            }
            else
            {
                root.context.getColorCompat(android.R.color.background_light)
            }
            root.setBackgroundColor(backgroundColor)
            item.form.values.imageUrl.url // todo: display it?

        }
    }

    interface Listener
    {
        fun onClick(item:FormViewHolderModel)
        fun onLongClick(item:FormViewHolderModel)
    }
}