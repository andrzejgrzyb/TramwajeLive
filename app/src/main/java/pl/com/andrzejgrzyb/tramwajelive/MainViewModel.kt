package pl.com.andrzejgrzyb.tramwajelive

import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawRepository

private const val TAG = "MainViewModel"

class MainViewModel(private val warsawRepository: WarsawRepository): BaseViewModel() {

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
}