package com.github.ericytsang.app.dynamicforms

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
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
        viewModel.listItemSelection.observe(this)
        {
            formPk->
            formPk ?: return@observe
            if (findViewById<View>(R.id.detail_fragment) == null)
            {
                viewModel.selectOne(null)
                FormActivity.start(this@ListActivity,formPk)
            }
        }
    }
}
