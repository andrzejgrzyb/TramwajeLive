package pl.com.andrzejgrzyb.tramwajelive.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FilterRepository {

    var inMemoryFilters = HashSet<String>()

    fun getFilters() : LiveData<HashSet<String>> {
        val liveData = MutableLiveData<HashSet<String>>()
        liveData.value = inMemoryFilters
        return liveData
    }

    fun addLine(lineNumber: String) {
        inMemoryFilters.add(lineNumber)
    }

    fun removeLine(lineNumber: String) {
        inMemoryFilters.remove(lineNumber)
    }
}