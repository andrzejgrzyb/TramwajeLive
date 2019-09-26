package pl.com.andrzejgrzyb.tramwajelive.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlin.math.min

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
            if (filterViewModel.addedLineFilter) {
                filter_sets_chip_group.setCheckChipIndex(filters.size - 1)
            } else {
                filterViewModel.checkedChip.value?.let {
                    filter_sets_chip_group.setCheckChipIndex(it)
                }
            }
        }
    }

    private fun addLineFilterChip(it: LinesFilter) {
        var text = it.lines.sortedWith(lineNumberComparator).subList(0, min(5, it.lines.size))
            .joinToString(separator = ",")
        if (it.lines.size > 5)
            text += "..."

        addLineFilterChip(text, getChipColor(it.color))
    }

    private fun addLineFilterChip(text: String, @ColorInt color: Int, checkable : Boolean = true) {
        val chip = Chip(filter_sets_chip_group.context)
            //R.style.Widget_MaterialComponents_ChipGroup)
        chip.apply {
            this.text = if (text.isEmpty()) "(empty)" else text
            isCheckable = checkable
            chipBackgroundColor = ColorStateList.valueOf(color)
            isCloseIconVisible = chip.isChecked
            isClickable = true
            setPadding(24, 16,24, 16)
            setOnCheckedChangeListener(onChipCheckedChangeListener)
            setOnCloseIconClickListener(onCloseClickListener)
        }
        filter_sets_chip_group.addView(chip, filter_sets_chip_group.childCount - 1)
    }

    private val onChipCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { chip, isChecked ->
            chip as Chip
            chip.isCloseIconVisible = isChecked

        }

    private val onCloseClickListener = View.OnClickListener {
        Log.i("OnCloseIconClickListene", "close icon clicked in chip " + (it as Chip).text)
        filterViewModel.checkedChip.value = -1
        mainViewModel.filteredLineNumbers.value = null
        mainViewModel.filtersEnabled.value = false
        filterViewModel.removeLineFilter(filter_sets_chip_group.indexOfChild(it))
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
            mainViewModel.filtersEnabled.value = true
            mainViewModel.filteredLineNumbers.value =
                filterViewModel.lineFilters.value?.get(checkedChip)?.lines
        } else {
            enableCheckboxes(false)
            mainViewModel.filtersEnabled.value = false
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