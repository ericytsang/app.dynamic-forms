package com.github.ericytsang.app.dynamicforms

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.github.ericytsang.app.dynamicforms.utils.exhaustive
import com.github.ericytsang.app.dynamicforms.viewmodel.FormDetailFragmentViewModel
import com.github.ericytsang.app.dynamicforms.viewmodel.FormDetailFragmentViewModelFactory
import com.github.ericytsang.app.dynamicforms.viewmodel.MainActivityViewModel
import com.github.ericytsang.app.dynamicforms.viewmodel.MainActivityViewModelFactory


class ListActivity:
    AppCompatActivity(),
    FormDetailFragmentViewModelFactory,
    MainActivityViewModelFactory
{
    override fun getFormDetailFragmentViewModel():FormDetailFragmentViewModel
    {
        return getMainActivityViewModel().formDetailFragmentViewModel
    }

    override fun getMainActivityViewModel():MainActivityViewModel
    {
        return InjectorUtils.getMainActivityViewModelInstance(this)
    }

    override fun onCreate(savedInstanceState:Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__list)

        val viewModel = getMainActivityViewModel()
        viewModel.formDetailFragmentViewModel.formDetails.observe(this)
        {
            formDetailState->

            when(formDetailState)
            {
                FormDetailFragmentViewModel.FormDetailState.Idle -> Unit
                is FormDetailFragmentViewModel.FormDetailState.Edit ->
                {
                    if (findViewById<View>(R.id.detail_fragment) == null)
                    {
                        viewModel.selectOne(null)
                        val params = if (formDetailState.original.formPk != null)
                        {
                            FormActivity.Params.Edit(formDetailState.original.formPk)
                        }
                        else
                        {
                            FormActivity.Params.New()
                        }
                        FormActivity.start(this@ListActivity,params)
                    }
                    Unit
                }
            }.exhaustive
        }
    }
}
