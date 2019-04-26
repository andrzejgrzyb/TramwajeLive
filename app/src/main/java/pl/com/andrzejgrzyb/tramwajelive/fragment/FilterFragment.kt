package pl.com.andrzejgrzyb.tramwajelive.fragment

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_filter.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.com.andrzejgrzyb.tramwajelive.MainViewModel
import pl.com.andrzejgrzyb.tramwajelive.R
import pl.com.andrzejgrzyb.tramwajelive.databinding.FragmentFilterBinding

class FilterFragment : Fragment() {

    private val vehicleDataViewModel by sharedViewModel<VehicleDataViewModel>()
    private val filterViewModel: FilterViewModel by viewModel()
    private val mainViewModel by sharedViewModel<MainViewModel>()

    companion object {
        val instance: FilterFragment by lazy { FilterFragment() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentFilterBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_filter, container, false)
        binding.lifecycleOwner = this
        binding.vm = filterViewModel
        val view = binding.root

        filterViewModel.lineSearch.observe(this, Observer { filter ->
            Log.i("FilterFragment", filter)
            if (filter.isNotEmpty()) {
                filter_checkboxes_container.children.forEach {
                    if ((it as CheckBox).text.contains(filter)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
            } else {
                filter_checkboxes_container.children.forEach {
                    it.visibility = View.VISIBLE
                }
            }
        })
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vehicleDataViewModel.lineNumbers.forEach {
            val checkBox = CheckBox(ContextThemeWrapper(context, R.style.FilterCheckbox), null, R.style.FilterCheckbox)
            checkBox.text = it
            checkBox.isChecked = filterViewModel.checkedLines?.contains(it) ?: false
            checkBox.setOnCheckedChangeListener(lineCheckedChangeListener)
            filter_checkboxes_container.addView(checkBox)
        }
    }

    private val lineCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            mainViewModel.addLineToFilters(buttonView.text.toString())
        } else {
            mainViewModel.removeLineFromFilters(buttonView.text.toString())
        }
    }
}