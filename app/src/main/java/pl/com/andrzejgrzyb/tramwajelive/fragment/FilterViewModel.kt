package pl.com.andrzejgrzyb.tramwajelive.fragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import pl.com.andrzejgrzyb.tramwajelive.BaseViewModel
import pl.com.andrzejgrzyb.tramwajelive.model.LinesFilter
import pl.com.andrzejgrzyb.tramwajelive.repository.FilterRepository

class FilterViewModel(private val filterRepository: FilterRepository) : BaseViewModel() {

    val lineSearch = MutableLiveData<String>()

    val checkedChip = MutableLiveData<Int>()

    fun isAnyChipChecked() : Boolean {
        checkedChip.value?.let {
            return it > -1
        }
        return false
    }

    fun getCurrentLineFilter() : LinesFilter? {
        return if (isAnyChipChecked()) {
            lineFilters.value?.get(checkedChip.value!!)
        } else {
            null
        }
    }

    val lineFilters = filterRepository.loadLineFilters()
    val checkedLines = MutableLiveData<HashSet<String>>()

    fun onAddLineFilterButtonClicked() {
        Log.i("FilterViewModel", "onAddLineFilterButtonClicked()")
        filterRepository.addNewLinesFilter()
    }

    fun removeLineFilter(index: Int) {
        lineFilters.value?.let {
            filterRepository.removeLinesFilter(it[index])
        }
    }

    fun addLineToCurrentFilter(lineNumber: String) {
        getCurrentLineFilter()?.let {
            it.addToLines(lineNumber)
            filterRepository.updateLinesFilter(it)
        }
    }

    fun removeLineFromCurrentFilter(lineNumber: String) {
        getCurrentLineFilter()?.let {
            it.lines.remove(lineNumber)
            filterRepository.updateLinesFilter(it)
        }
    }
}