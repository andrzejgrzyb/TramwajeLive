package pl.com.andrzejgrzyb.tramwajelive.fragment

import androidx.lifecycle.MutableLiveData
import pl.com.andrzejgrzyb.tramwajelive.BaseViewModel
import pl.com.andrzejgrzyb.tramwajelive.repository.FilterRepository

class FilterViewModel(filterRepository: FilterRepository) : BaseViewModel() {

    val lineSearch = MutableLiveData<String>()
    val checkedLines = filterRepository.getFilters().value
}