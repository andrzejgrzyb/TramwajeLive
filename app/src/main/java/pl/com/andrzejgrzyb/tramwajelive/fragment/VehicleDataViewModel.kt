package pl.com.andrzejgrzyb.tramwajelive.fragment

import pl.com.andrzejgrzyb.tramwajelive.BaseViewModel
import pl.com.andrzejgrzyb.tramwajelive.repository.FilterRepository
import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawRepository

class VehicleDataViewModel(warsawRepository: WarsawRepository, filterRepository: FilterRepository) : BaseViewModel() {

    val vehicleData = warsawRepository.allVehicleResults
    val lineNumbers: List<String>
        get() {
            val lines = HashSet<String>()
            vehicleData.value?.forEach { lines.add(it.lines) }
            return lines.toList().sortedWith(Comparator<String> { a, b ->
                val aAsInt = a.toIntOrNull()
                val bAsInt = b.toIntOrNull()
                when {
                    aAsInt == null && bAsInt == null -> a.compareTo(b)
                    aAsInt == null && bAsInt != null -> 1
                    aAsInt != null && bAsInt == null -> -1
                    aAsInt != null && bAsInt != null -> aAsInt.compareTo(bAsInt)
                    else -> 0
                }
            })
        }

    init {
        toastMessage = warsawRepository.errorMessage
    }
}