package pl.com.andrzejgrzyb.tramwajelive.fragment

import com.google.android.gms.maps.model.CameraPosition
import pl.com.andrzejgrzyb.tramwajelive.BaseViewModel
import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawRepository

class VehicleDataViewModel(warsawRepository: WarsawRepository) : BaseViewModel() {

    init {
        toastMessage = warsawRepository.errorMessage
    }

    var cameraPosition: CameraPosition? = null
    // TODO remove vehicleData list
    val vehicleData = warsawRepository.allVehicleResults
    val vehicleMap = warsawRepository.vehicleMapLiveData

    val lineNumbers: List<String>
        get() {
            val lines = HashSet<String>()
            vehicleData.value?.forEach { lines.add(it.line) }
            return lines.toList().sortedWith(Comparator<String> { a, b ->
                val aAsInt = a.toIntOrNull()
                val bAsInt = b.toIntOrNull()
                if (aAsInt == null) {
                    if (bAsInt == null) a.compareTo(b)
                    else 1
                } else {
                    if (bAsInt == null) -1
                    else aAsInt.compareTo(bAsInt)
                }
            })
        }
}