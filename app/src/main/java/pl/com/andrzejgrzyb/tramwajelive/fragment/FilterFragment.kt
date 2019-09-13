package pl.com.andrzejgrzyb.tramwajelive.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.annotation.ColorInt
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_filter.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.com.andrzejgrzyb.tramwajelive.MainViewModel
import pl.com.andrzejgrzyb.tramwajelive.R
import pl.com.andrzejgrzyb.tramwajelive.adapter.setCheckChipIndex
import pl.com.andrzejgrzyb.tramwajelive.databinding.FragmentFilterBinding
import pl.com.andrzejgrzyb.tramwajelive.model.LinesFilter
import pl.com.andrzejgrzyb.tramwajelive.usecase.GetVehicleDataUseCase.Companion.lineNumberComparator

class FilterFragment : Fragment() {

    private val vehicleDataViewModel by sharedViewModel<MapViewModel>()
    private val filterViewModel: FilterViewModel by viewModel()
    private val mainViewModel by sharedViewModel<MainViewModel>()
    private lateinit var binding: FragmentFilterBinding

    companion object {
        val instance: FilterFragment by lazy { FilterFragment() }
    }

    private val lineSearchObserver = Observer { filter: String ->
        Log.i("FilterFragment", filter)

        if (filter.isNotEmpty()) {
            filter_checkboxes_container.children.forEach {
                it.visibility = if ((it as CheckBox).text.contains(filter)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        } else {
            filter_checkboxes_container.children.forEach { it.visibility = View.VISIBLE }
        }
    }

    private val lineFiltersObserver = Observer { filters : List<LinesFilter> ->
        Log.i("lineFiltersObserver", filters.toString())
        filter_sets_chip_group.removeViews(0, filter_sets_chip_group.childCount - 1)
        if (filters.isNotEmpty()) {
            filters.forEach {
                addLineFilterChip(it)
            }
            filterViewModel.checkedChip.value?.let {
                filter_sets_chip_group.setCheckChipIndex(it)
            }
        }
    }

    private fun addLineFilterChip(it: LinesFilter) {
        addLineFilterChip(it.lines.sortedWith(lineNumberComparator).joinToString(separator = ","), getChipColor(it.color))
    }

    private fun addLineFilterChip(text: String, @ColorInt color: Int, checkable : Boolean = true) {
        val chip = Chip(filter_sets_chip_group.context, null, R.style.Widget_MaterialComponents_ChipGroup)
        chip.text = if (text.isEmpty()) "(empty)" else text
        chip.isCheckable = checkable
        chip.chipBackgroundColor = ColorStateList.valueOf(color)
        chip.setOnLongClickListener {
            filterViewModel.checkedChip.value?.let {checkChipIndex ->

                val currentChipIndex = filter_sets_chip_group.indexOfChild(it)
                if (checkChipIndex > currentChipIndex) {
                    filterViewModel.checkedChip.value = checkChipIndex - 1
                } else if (checkChipIndex == currentChipIndex) {
                    filterViewModel.checkedChip.value = -1
                }
            }

            filterViewModel.removeLineFilter(filter_sets_chip_group.indexOfChild(it))
            return@setOnLongClickListener true
        }
        filter_sets_chip_group.addView(chip, filter_sets_chip_group.childCount - 1)
    }

    private fun getChipColor(colorNumber: Int) : Int {
        return context!!.resources.getIntArray(R.array.rainbow)[colorNumber]
    }

    private fun enableCheckboxes(enabled: Boolean) {
        filter_checkboxes_container.children.forEach {
            it.isEnabled = enabled
        }
    }

    private val checkedChipObserver = Observer { checkedChip: Int ->
        Log.i("FilterFragment", "checkedChip: $checkedChip")
        if (checkedChip > -1 && checkedChip < filterViewModel.lineFilters.value?.size?: Int.MAX_VALUE ) {
            val linesFilter = filterViewModel.lineFilters.value?.get(checkedChip)
            Log.i("currentLineFilterObserv", linesFilter.toString())
            enableCheckboxes(true)
            mainViewModel.filteredLineNumbers.value =
                filterViewModel.lineFilters.value?.get(checkedChip)?.lines
        } else {
            enableCheckboxes(false)
            mainViewModel.filteredLineNumbers.value = null
        }
        updateCheckboxes()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_filter, container, false)
        binding.lifecycleOwner = this
        binding.vm = filterViewModel
        binding.executePendingBindings()
        val view = binding.root

        filterViewModel.lineSearch.observe(viewLifecycleOwner, lineSearchObserver)
        filterViewModel.lineFilters.observe(viewLifecycleOwner, lineFiltersObserver)
        filterViewModel.checkedChip.observe(viewLifecycleOwner, checkedChipObserver)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        filterViewModel.lineSearch.postValue("")
        vehicleDataViewModel.lineNumbers.forEach {
            val checkBox = CheckBox(ContextThemeWrapper(context, R.style.FilterCheckbox), null, R.style.FilterCheckbox)
            checkBox.text = it
            checkBox.isChecked = filterViewModel.checkedLines.value?.contains(it) ?: false
            checkBox.setOnCheckedChangeListener(lineCheckedChangeListener)
            filter_checkboxes_container.addView(checkBox)
        }
        enableCheckboxes(filterViewModel.isAnyChipChecked())
    }

    private val lineCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            filterViewModel.addLineToCurrentFilter(buttonView.text.toString())
        } else {
            filterViewModel.removeLineFromCurrentFilter(buttonView.text.toString())
        }
    }

    private fun updateCheckboxes() {
        filterViewModel.getCurrentLineFilter()?.lines?.let { lines ->
            filter_checkboxes_container.children.forEach {
                it as CheckBox
                it.isChecked = lines.contains(it.text)
            }
        }
    }
}