package pl.com.andrzejgrzyb.tramwajelive

import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomnavigation.BottomNavigationView
import pl.com.andrzejgrzyb.tramwajelive.repository.FilterRepository

private const val TAG = "MainViewModel"

class MainViewModel(private val filterRepository: FilterRepository) :
    BaseViewModel() {

    val filteredLineNumbers = MutableLiveData<Set<String>>()

    val currentFragment = MutableLiveData<Int>().apply { postValue(R.id.navigation_home) }

    val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        currentFragment.postValue(item.itemId)
        return@OnNavigationItemSelectedListener true
    }

    fun refreshData() {
        // TODO manual refresh
        toastMessage.value = "Manual refresh"
    }

    fun isFilterOn(): Boolean = !filteredLineNumbers.value.isNullOrEmpty()

    fun filterButtonClicked() {
        if (isFilterOn()) {
            filteredLineNumbers.postValue(null)
        } else {
            currentFragment.postValue(R.id.navigation_lines)
        }
    }
}