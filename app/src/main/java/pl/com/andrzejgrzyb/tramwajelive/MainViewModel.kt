package pl.com.andrzejgrzyb.tramwajelive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.com.andrzejgrzyb.tramwajelive.repository.FilterRepository
import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawRepository

private const val TAG = "MainViewModel"

class MainViewModel(private val warsawRepository: WarsawRepository, val filterRepository: FilterRepository): BaseViewModel() {

    val filteredLineNumbers = MutableLiveData<HashSet<String>>()

    fun startRefreshingData() {
        warsawRepository.startRefreshingData()
    }

    fun stopRefreshingData() {
        warsawRepository.stopRefreshingData()
    }

    fun refreshData() {
        warsawRepository.startRefreshingData()
        toastMessage.value = "Manual refresh"
    }

    fun addLineToFilters(lineNumber: String) {
        filterRepository.addLine(lineNumber)
    }
    fun removeLineFromFilters(lineNumber: String) {
        filterRepository.removeLine(lineNumber)
    }

    fun isFilterOn(): Boolean = filteredLineNumbers.value != null

    fun filterButtonClicked() {
        if (isFilterOn()) {
            filteredLineNumbers.postValue(null)
        } else {
            filteredLineNumbers.postValue(filterRepository.getFilters().value)
        }
    }
}