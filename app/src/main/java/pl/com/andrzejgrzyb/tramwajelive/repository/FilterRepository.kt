package pl.com.andrzejgrzyb.tramwajelive.repository

import io.objectbox.android.ObjectBoxLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import pl.com.andrzejgrzyb.tramwajelive.model.LinesFilter
import pl.com.andrzejgrzyb.tramwajelive.model.LinesFilter_
import pl.com.andrzejgrzyb.tramwajelive.repository.ObjectBox.boxStore

class FilterRepository {

    private inline fun <reified T : Any> saveToDatabase(data: List<T>) {
        CoroutineScope(Dispatchers.IO).launch {
            boxStore.boxFor(T::class.java).put(data)
        }
    }

    private inline fun <reified T : Any> saveToDatabase(data: T) {
        CoroutineScope(Dispatchers.IO).launch {
            boxStore.boxFor(T::class.java).put(data)
        }
    }

    private inline fun <reified T : Any> removeFromDatabase(data: T) {
        CoroutineScope(Dispatchers.IO).launch {
            boxStore.boxFor(T::class.java).remove(data)
        }
    }

    fun loadLineFilters(): ObjectBoxLiveData<LinesFilter> {
        val box = boxStore.boxFor(LinesFilter::class.java)
        if (box.isEmpty) {
            box.put(LinesFilter(0, 0, HashSet()))
        }
        return ObjectBoxLiveData<LinesFilter>(
            boxStore.boxFor(LinesFilter::class.java).query().order(LinesFilter_.id).build()
        )
    }

    fun addNewLinesFilter() {
        saveToDatabase(LinesFilter(0, 0, HashSet()))
    }

    fun removeLinesFilter(linesFilter: LinesFilter) {
        removeFromDatabase(linesFilter)
    }

    fun updateLinesFilter(linesFilter: LinesFilter) {
        saveToDatabase(linesFilter)
    }
}