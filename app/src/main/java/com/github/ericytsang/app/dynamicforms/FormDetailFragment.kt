package com.github.ericytsang.app.dynamicforms

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.ericytsang.app.dynamicforms.database.FormEntity
import com.github.ericytsang.app.dynamicforms.databinding.LayoutListWithFabBinding
import com.github.ericytsang.app.dynamicforms.databinding.ListItemDateFormFieldBinding
import com.github.ericytsang.app.dynamicforms.databinding.ListItemTextFormFieldBinding
import com.github.ericytsang.app.dynamicforms.domainobjects.FormField
import com.github.ericytsang.app.dynamicforms.utils.TextWatcherAdapter
import com.github.ericytsang.app.dynamicforms.viewmodel.MainActivityViewModel
import java.util.Calendar


class FormDetailFragment:Fragment()
{
    override fun onCreateView(
        inflater:LayoutInflater,
        container:ViewGroup?,
        savedInstanceState:Bundle?
    ):View?
    {
        val viewBinding = LayoutListWithFabBinding.inflate(inflater,container,false)
        val viewModel = InjectorUtils.getMainActivityViewModel(activity!!)

        val listener = object:FormFieldViewHolder.Listener
        {
            init
            {
                // publish form field modifications to the view model
                viewModel.formDetails.observe(viewLifecycleOwner)
                {
                    onChangedDelegate = fun(model:FormFieldViewHolderModel)
                    {
                        viewModel.publishPendingChanges(model)
                    }
                }
            }

            private var onChangedDelegate:(model:FormFieldViewHolderModel)->Unit = {}
            override fun onChanged(model:FormFieldViewHolderModel) = onChangedDelegate(model)
        }
        viewBinding.recyclerView.adapter = FormFieldAdapter(listener).apply()
        {
            // fixme: this glitches the typing
            viewModel.formDetails.observe(viewLifecycleOwner)
            {formDetailsState ->
                when (formDetailsState)
                {
                    MainActivityViewModel.FormDetailState.Idle -> submitList(listOf()) // todo: show empty state
                    is MainActivityViewModel.FormDetailState.Edit ->
                    {
                        this@FormDetailFragment.debugLog {"submitList(${formDetailsState.unsavedChanges})"}
                        submitList(formDetailsState.unsavedChanges)
                    }
                }.exhaustive
            }
        }
        viewBinding.fab.setImageDrawable(context!!.getDrawableCompat(R.drawable.ic_save_black_24dp))
        // todo: add ability to check whether the form was modified or not so we can confirm whether user wants to discard their changes

        viewModel.formDetails.observe(viewLifecycleOwner)
        {
            val allowSaving = when (it)
            {
                MainActivityViewModel.FormDetailState.Idle -> null
                is MainActivityViewModel.FormDetailState.Edit -> if (it.hasBeenModified) it else null
            }.exhaustive

            if (allowSaving != null)
            {
                viewBinding.fab.show()
                viewBinding.fab.setOnClickListener()
                {view:View ->
                    viewModel.saveForm(view.context!!,allowSaving)
                }
            }
            else
            {
                viewBinding.fab.hide()
                viewBinding.fab.setOnClickListener {}
            }
        }

        return viewBinding.root
    }

    override fun onResume()
    {
        super.onResume()

    }
}


private class FormFieldAdapter(
    private val listener:FormFieldViewHolder.Listener
):
    ListAdapter<FormFieldViewHolderModel,FormFieldViewHolder>(diffCallback)
{
    override fun getItemViewType(position:Int):Int
    {
        return when (getItem(position))
        {
            is FormFieldViewHolderModel.Text -> FormFieldViewHolderType.TEXT.ordinal
            is FormFieldViewHolderModel.Date -> FormFieldViewHolderType.DATE.ordinal
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
                toBindToViewHolder as FormFieldViewHolderModel.Text
                holder.bind(listener,toBindToViewHolder)
            }
            is FormFieldViewHolder.DateFormFieldViewHolder ->
            {
                toBindToViewHolder as FormFieldViewHolderModel.Date
                holder.bind(listener,toBindToViewHolder)
            }
        }.exhaustive
    }

    companion object
    {
        private val diffCallback = object:DiffUtil.ItemCallback<FormFieldViewHolderModel>()
        {
            override fun areItemsTheSame(
                oldItem:FormFieldViewHolderModel,
                newItem:FormFieldViewHolderModel
            ):Boolean
            {
                return oldItem.positionInForm == newItem.positionInForm
            }

            override fun areContentsTheSame(
                oldItem:FormFieldViewHolderModel,
                newItem:FormFieldViewHolderModel
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


sealed class FormFieldViewHolderModel
{
    companion object
    {
        fun fromModel(model:FormField):FormFieldViewHolderModel
        {
            return when (model)
            {
                is FormField.TextFormField -> Text(
                    model.values.positionInForm,
                    model.values.label,
                    model.values.isRequired,
                    model.values.userInput
                )
                is FormField.DateFormField -> Date(
                    model.values.positionInForm,
                    model.values.label,
                    model.values.isRequired,
                    model.values.userInput
                )
            }
        }
    }

    fun toModel(formPk:FormEntity.Pk):FormField.Values
    {
        return when (this)
        {
            is Text -> FormField.Values.Text(
                formPk,
                positionInForm,
                formFieldLabel,
                formFieldIsRequired,
                formFieldValue
            )
            is Date -> FormField.Values.Date(
                formPk,
                positionInForm,
                formFieldLabel,
                formFieldIsRequired,
                formFieldValue
            )
        }
    }

    /**
     * returns the user's input as a string to show to user.
     */
    val userInputAsString:String
        get()
        {
            return when (this)
            {
                is Text -> formFieldValue
                is Date -> formFieldValue?.toString() ?: "<no date set>"
            }
        }

    abstract val positionInForm:Int
    abstract val formFieldLabel:String
    abstract val formFieldIsRequired:Boolean
    abstract override fun hashCode():Int
    abstract override fun equals(other:Any?):Boolean

    data class Text(
        override val positionInForm:Int,
        override val formFieldLabel:String,
        override val formFieldIsRequired:Boolean,
        val formFieldValue:String
    ):FormFieldViewHolderModel()

    data class Date(
        override val positionInForm:Int,
        override val formFieldLabel:String,
        override val formFieldIsRequired:Boolean,
        val formFieldValue:Calendar?
    ):FormFieldViewHolderModel()
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

        fun bind(listener:Listener,model:FormFieldViewHolderModel.Text)
        {
            afterTextChangedDelegate = fun(s:Editable?)
            {
                listener.onChanged(model.copy(formFieldValue = s.toString()))
            }
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
        fun bind(listener:Listener,model:FormFieldViewHolderModel.Date)
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
        fun onChanged(model:FormFieldViewHolderModel)
    }
}
