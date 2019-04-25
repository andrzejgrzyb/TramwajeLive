package pl.com.andrzejgrzyb.tramwajelive.fragment

import pl.com.andrzejgrzyb.tramwajelive.BaseViewModel
import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawRepository

class VehicleDataViewModel(warsawRepository: WarsawRepository) : BaseViewModel() {

    val vehicleData = warsawRepository.allVehicleResults

    val lineNumbers: List<String>
        get() {
            val lines = HashSet<String>()
            vehicleData.value?.forEach { lines.add(it.lines) }
            return lines.toList().sortedWith(Comparator<String> { a, b ->
                when {
                    a.toIntOrNull() == null && b.toIntOrNull() == null -> a.compareTo(b)
                    a.toIntOrNull() == null && b.toIntOrNull() != null -> 1
                    a.toIntOrNull() != null && b.toIntOrNull() == null -> -1
                    a.toIntOrNull() != null && b.toIntOrNull() != null -> a.toInt().compareTo(b.toInt())
                    else -> 0
                }
            })
        }


    init {
        toastMessage = warsawRepository.errorMessage
    }
}