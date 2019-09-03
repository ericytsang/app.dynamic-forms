package com.github.ericytsang.app.dynamicforms

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.github.ericytsang.app.dynamicforms.utils.debugLog
import com.github.ericytsang.app.dynamicforms.utils.exhaustive
import com.github.ericytsang.app.dynamicforms.viewmodel.MainActivityViewModel


class ListActivity:AppCompatActivity()
{
    private val formActivityStarter = object
    {
        private var formOpenedForEditing:MainActivityViewModel.FormDetails? = null

        val observer get() = _observer
        private val _observer:Observer<MainActivityViewModel.FormDetailState> = Observer()
        {
            formDetailState:MainActivityViewModel.FormDetailState ->

            val viewModel = InjectorUtils.getMainActivityViewModel(this@ListActivity)

            when(formDetailState)
            {
                MainActivityViewModel.FormDetailState.Idle ->
                {
                    formOpenedForEditing = null
                }
                is MainActivityViewModel.FormDetailState.Edit ->
                {
                    if (findViewById<View>(R.id.detail_fragment) == null && formDetailState.original != formOpenedForEditing)
                    {
                        debugLog {"FormActivity.start(...)"}
                        // todo: fix opening the details page twice
                        FormActivity.start(this@ListActivity)
                        formOpenedForEditing = formDetailState.original
                        viewModel.formDetails.removeObserver(observer)
                    }
                    Unit
                }
            }.exhaustive
        }
    }

    override fun onCreate(savedInstanceState:Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__list)
    }

    override fun onResume()
    {
        super.onResume()
        val viewModel = InjectorUtils.getMainActivityViewModel(this)
        viewModel.formDetails.observe(this,formActivityStarter.observer)
    }
}
