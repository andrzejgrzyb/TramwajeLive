package pl.com.andrzejgrzyb.tramwajelive

import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomnavigation.BottomNavigationView
import pl.com.andrzejgrzyb.tramwajelive.repository.FilterRepository
import pl.com.andrzejgrzyb.tramwajelive.usecase.GetVehicleDataUseCase

private const val TAG = "MainViewModel"

class MainViewModel(
    private val filterRepository: FilterRepository,
    private val getVehicleDataUseCase: GetVehicleDataUseCase
) :
    BaseViewModel() {

    val filteredLineNumbers = MutableLiveData<Set<String>>()
    val filtersEnabled = MutableLiveData<Boolean>()

    val currentFragment = MutableLiveData<Int>().apply { postValue(R.id.navigation_home) }

    val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        currentFragment.postValue(item.itemId)
        return@OnNavigationItemSelectedListener true
    }

    fun refreshData() {
        getVehicleDataUseCase.refreshVehiclePositions()
        toastMessage.value = "Manual refresh"
    }

    fun filterButtonClicked() {
        if (filtersEnabled.value == true) {
            filtersEnabled.value = false
        } else if (!filteredLineNumbers.value.isNullOrEmpty()){
            filtersEnabled.value = true
        } else {
            currentFragment.postValue(R.id.navigation_lines)
        }
    }
}