package com.github.ericytsang.app.dynamicforms

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.map
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.ericytsang.app.dynamicforms.databinding.LayoutListWithFabBinding
import com.github.ericytsang.app.dynamicforms.databinding.ListItemDateFormFieldBinding
import com.github.ericytsang.app.dynamicforms.databinding.ListItemTextFormFieldBinding
import com.github.ericytsang.app.dynamicforms.domainobjects.FormFieldReadData
import com.github.ericytsang.app.dynamicforms.utils.StructEqualityAdapter
import com.github.ericytsang.app.dynamicforms.utils.TextWatcherAdapter
import com.github.ericytsang.app.dynamicforms.utils.debounced
import com.github.ericytsang.app.dynamicforms.utils.debugLog
import com.github.ericytsang.app.dynamicforms.utils.exhaustive
import com.github.ericytsang.app.dynamicforms.utils.getDrawableCompat
import com.github.ericytsang.app.dynamicforms.utils.layoutInflater
import com.github.ericytsang.app.dynamicforms.viewmodel.FormDetailFragmentViewModel
import com.github.ericytsang.app.dynamicforms.viewmodel.FormDetailFragmentViewModelFactory


class FormDetailFragment:Fragment()
{
    override fun onCreateView(
        inflater:LayoutInflater,
        container:ViewGroup?,
        savedInstanceState:Bundle?
    ):View?
    {
        val viewBinding = LayoutListWithFabBinding.inflate(inflater,container,false)
        val viewModel = (activity as FormDetailFragmentViewModelFactory).getFormDetailFragmentViewModel()

        val listener = object:FormFieldViewHolder.Listener
        {
            init
            {
                // publish form field modifications to the view model
                viewModel.formDetails.observe(viewLifecycleOwner)
                {
                    onChangedDelegate = fun(model:FormFieldReadData)
                    {
                        viewModel.publishPendingChanges(model)
                    }
                }
            }

            private var onChangedDelegate:(model:FormFieldReadData)->Unit = {}
            override fun onChanged(model:FormFieldReadData) = onChangedDelegate(model)
        }
        viewBinding.recyclerView.adapter = FormFieldAdapter(listener).apply()
        {
            viewModel.formDetails
                .map()
                {formDetailState ->
                    when (formDetailState)
                    {
                        FormDetailFragmentViewModel.FormDetailState.Idle ->
                            StructEqualityAdapter(
                                listOf<FormFieldReadData>(),
                                listOf()
                            ) // todo: show empty state
                        is FormDetailFragmentViewModel.FormDetailState.Edit ->
                            StructEqualityAdapter(
                                formDetailState.original.formFields,
                                formDetailState.unsavedChanges
                            )
                    }
                }
                .debounced()
                .observe(viewLifecycleOwner)
                {
                    debugLog {"submitList($it)"}
                    submitList(it.value)
                }
        }
        viewBinding.fab.setImageDrawable(context!!.getDrawableCompat(R.drawable.ic_save_black_24dp))
        // todo: add ability to check whether the form was modified or not so we can confirm whether user wants to discard their changes

        viewModel.formDetails.observe(viewLifecycleOwner)
        {
            val allowSaving = when (it)
            {
                FormDetailFragmentViewModel.FormDetailState.Idle -> null
                is FormDetailFragmentViewModel.FormDetailState.Edit -> if (it.canSave) it else null
            }.exhaustive

            if (allowSaving != null)
            {
                viewBinding.fab.show()
                viewBinding.fab.setOnClickListener()
                {view:View ->
                    viewModel.saveForm(view.context!!,allowSaving)
                }
            } else
            {
                viewBinding.fab.hide()
                viewBinding.fab.setOnClickListener {}
            }
        }

        return viewBinding.root
    }
}


private class FormFieldAdapter(
    private val listener:FormFieldViewHolder.Listener
):
    ListAdapter<FormFieldReadData,FormFieldViewHolder>(diffCallback)
{
    override fun getItemViewType(position:Int):Int
    {
        return when (getItem(position))
        {
            is FormFieldReadData.Text -> FormFieldViewHolderType.TEXT.ordinal
            is FormFieldReadData.Date -> FormFieldViewHolderType.DATE.ordinal
        }
    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):FormFieldViewHolder
    {
        return FormFieldViewHolderType.values()[viewType].makeViewHolder(parent)
    }

    override fun onBindViewHolder(holder:FormFieldViewHolder,position:Int)
    {
        val toBindToViewHolder = getItem(position)
        when (holder)
        {
            is FormFieldViewHolder.TextFormFieldViewHolder ->
            {
                toBindToViewHolder as FormFieldReadData.Text
                holder.bind(listener,toBindToViewHolder)
            }
            is FormFieldViewHolder.DateFormFieldViewHolder ->
            {
                toBindToViewHolder as FormFieldReadData.Date
                holder.bind(listener,toBindToViewHolder)
            }
        }.exhaustive
    }

    companion object
    {
        private val diffCallback = object:DiffUtil.ItemCallback<FormFieldReadData>()
        {
            override fun areItemsTheSame(
                oldItem:FormFieldReadData,
                newItem:FormFieldReadData
            ):Boolean
            {
                return oldItem.positionInForm == newItem.positionInForm
            }

            override fun areContentsTheSame(
                oldItem:FormFieldReadData,
                newItem:FormFieldReadData
            ):Boolean
            {
                return oldItem == newItem
            }
        }
    }
}


private enum class FormFieldViewHolderType
{
    TEXT,
    DATE,
    ;

    fun makeViewHolder(parent:ViewGroup):FormFieldViewHolder
    {
        val layoutInflater = parent.context.layoutInflater
        return when (this)
        {
            TEXT -> FormFieldViewHolder.TextFormFieldViewHolder(
                ListItemTextFormFieldBinding.inflate(layoutInflater,parent,false)
            )
            DATE -> FormFieldViewHolder.DateFormFieldViewHolder(
                ListItemDateFormFieldBinding.inflate(layoutInflater,parent,false)
            )
        }
    }
}


private sealed class FormFieldViewHolder(view:View):RecyclerView.ViewHolder(view)
{

    class TextFormFieldViewHolder(
        private val viewBinding:ListItemTextFormFieldBinding
    ):FormFieldViewHolder(viewBinding.root)
    {
        private var afterTextChangedDelegate:(s:Editable?)->Unit = {}

        init
        {
            viewBinding.editText.addTextChangedListener(object:TextWatcherAdapter
            {
                override fun afterTextChanged(s:Editable?) = afterTextChangedDelegate(s)
            })
        }

        fun bind(listener:Listener,model:FormFieldReadData.Text)
        {
            afterTextChangedDelegate = fun(s:Editable?)
            {
                val newModel = model.copy(formFieldValue = s.toString())

                // validate & update UI with validation info
                viewBinding.editText.error = if (!newModel.isValid)
                {
                    viewBinding.root.context.getString(R.string.field_is_required, model.formFieldLabel)
                }
                else
                {
                    null
                }

                // publish updates to listeners
                listener.onChanged(newModel)

            }.apply() // initialize UI
            {
                invoke(viewBinding.editText.text)
            }

            // bind model data to view
            viewBinding.apply()
            {
                layout.hint = model.formFieldLabel
                editText.setText(model.formFieldValue)
            }
        }
    }

    class DateFormFieldViewHolder(
        private val viewBinding:ListItemDateFormFieldBinding
    ):FormFieldViewHolder(viewBinding.root)
    {
        fun bind(listener:Listener,model:FormFieldReadData.Date)
        {
            viewBinding.apply()
            {
                // todo
                // root.setOnClickListener {listener.onChanged(model)}
//                title.text = item.values.title
//                description.text = item.values.description
//                item.values.imageUrl.url
            }
        }
    }

    interface Listener
    {
        fun onChanged(model:FormFieldReadData)
    }
}
