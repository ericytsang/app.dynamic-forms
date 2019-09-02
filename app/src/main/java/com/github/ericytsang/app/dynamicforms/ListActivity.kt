package com.github.ericytsang.app.dynamicforms

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.github.ericytsang.app.dynamicforms.viewmodel.MainActivityViewModel


class ListActivity:AppCompatActivity()
{
    private var formOpenedForEditing:MainActivityViewModel.FormDetails? = null

    override fun onCreate(savedInstanceState:Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__list)

        val viewModel = InjectorUtils.getMainActivityViewModel(this)

        // do to FormActivity if the detail fragment is not available (i.e. portrait mode)
        viewModel.formDetails.observe(this)
        {
            when(it)
            {
                MainActivityViewModel.FormDetailState.Idle ->
                {
                    formOpenedForEditing = null
                }
                is MainActivityViewModel.FormDetailState.Edit ->
                {
                    if (findViewById<View>(R.id.detail_fragment) == null && it.original != formOpenedForEditing)
                    {
                        debugLog {"FormActivity.start(...)"}
                        // todo: fix opening the details page twice
                        FormActivity.start(this)
                        formOpenedForEditing = it.original
                    }
                    Unit
                }
            }.exhaustive
        }
    }

    override fun onResume()
    {
        super.onResume()
        formOpenedForEditing = null
    }
}
