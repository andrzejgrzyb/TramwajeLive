package pl.com.andrzejgrzyb.tramwajelive.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pl.com.andrzejgrzyb.tramwajelive.model.ApiResponse
import pl.com.andrzejgrzyb.tramwajelive.model.Result
import pl.com.andrzejgrzyb.tramwajelive.model.Vehicle
import pl.com.andrzejgrzyb.tramwajelive.model.VehicleInfo
import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawRepository
import java.text.SimpleDateFormat

class GetVehicleDataUseCase(private val warsawRepository: WarsawRepository) {

    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val _vehiclePositions = MutableLiveData<MutableMap<String, Vehicle>>()
    val vehiclePositions: LiveData<MutableMap<String, Vehicle>>
        get() = _vehiclePositions
    var lineNumbers = emptyList<String>()

    fun refreshVehiclePositions() {
        GlobalScope.launch {
            val trams = async { warsawRepository.getAllVehicles(true) }
            val buses = async { warsawRepository.getAllVehicles(false) }
            val tramResults = getDataOrEmptyList(trams.await())
            val busResults = getDataOrEmptyList(buses.await())
            val summedResults = sumVehicleResults(tramResults, busResults)
            _vehiclePositions.postValue(summedResults)
        }
    }

    private fun getDataOrEmptyList(apiResult: Result<ApiResponse>): List<VehicleInfo> {
        return when (apiResult) {
            is Result.Success -> apiResult.data.result ?: emptyList()
            else -> emptyList()
        }
    }

    private fun sumVehicleResults(
        tramResults: List<VehicleInfo>,
        busResults: List<VehicleInfo>
    ): MutableMap<String, Vehicle> {
        val currentTime = System.currentTimeMillis()
        val data = tramResults.filter { currentTime < format.parse(it.time).time + 120 * 1000 } +
                busResults.filter { currentTime < format.parse(it.time).time + 120 * 1000 }
        val vehiclesMap = _vehiclePositions.value ?: HashMap()
        val updatedLineNumbers = HashSet<String>()
        data.forEach {
            it.apply {
                updatedLineNumbers.add(line)
                vehiclesMap[markerKey]?.updateLocation(lat, lon, time) ?: vehiclesMap.put(
                    markerKey,
                    Vehicle(lat, lon, time, line, brigade)
                )
            }
        }
        lineNumbers = updatedLineNumbers.sortedWith(comparator)
        return vehiclesMap
    }

    private val comparator = Comparator<String> { a, b ->
        val aAsInt = a.toIntOrNull()
        val bAsInt = b.toIntOrNull()
        if (aAsInt == null) {
            if (bAsInt == null) a.compareTo(b)
            else 1
        } else {
            if (bAsInt == null) -1
            else aAsInt.compareTo(bAsInt)
        }
    }
}