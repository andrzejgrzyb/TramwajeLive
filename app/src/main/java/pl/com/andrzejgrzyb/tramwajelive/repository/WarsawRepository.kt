package pl.com.andrzejgrzyb.tramwajelive.repository

import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.com.andrzejgrzyb.tramwajelive.model.ApiResponse
import pl.com.andrzejgrzyb.tramwajelive.model.Result
import pl.com.andrzejgrzyb.tramwajelive.model.Vehicle
import pl.com.andrzejgrzyb.tramwajelive.model.VehicleInfo
import java.text.SimpleDateFormat
import kotlin.coroutines.CoroutineContext

const val API_CALL_INTERVAL = 10000L

class WarsawRepository(private val warsawService: WarsawService) : BaseRepository() {

    private val TAG = "WarsawRepository"
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private var tramResults: List<VehicleInfo> = emptyList()
        set(value) {
            field = value
            if (!busCallInProgress) {
                sumVehicleResults()
            }
        }

    private var busResults: List<VehicleInfo> = emptyList()
        set(value) {
            field = value
            if (!tramCallInProgress) {
                sumVehicleResults()
            }
        }

    private fun sumVehicleResults() {
        val currentTime = System.currentTimeMillis()
        val data = tramResults.filter { currentTime < format.parse(it.time).time + 120 * 1000 } +
                busResults.filter { currentTime < format.parse(it.time).time + 120 * 1000 }
        data.forEach {
            it.apply {
                vehiclesMap[markerKey]?.updateLocation(lat, lon, time) ?: vehiclesMap.put(
                    markerKey,
                    Vehicle(lat, lon, time, line, brigade)
                )
            }
        }
        vehicleMapLiveData.postValue(vehiclesMap)
        // TODO remove this variable
        allVehicleResults.postValue(data)
    }

    private var tramCallInProgress = false
    private var busCallInProgress = false

    var allVehicleResults = MutableLiveData<List<VehicleInfo>>()
    private val vehiclesMap = HashMap<String, Vehicle>()
    val vehicleMapLiveData = MutableLiveData<HashMap<String, Vehicle>>()

    private val parentJob = Job()
    private val busParentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default
    private val busCoroutineContext: CoroutineContext
        get() = busParentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)
    private val busScope = CoroutineScope(busCoroutineContext)
    private val handler = Handler()

    private val runnableCode = object : Runnable {
        override fun run() {
            Log.i(TAG, "in runnable")
            // Do something here on the main thread
            getTramData()
            getBusData()
            handler.postDelayed(this, API_CALL_INTERVAL)
        }
    }

    fun startRefreshingData() {
        handler.removeCallbacks(runnableCode)
        handler.post(runnableCode)
    }

    fun stopRefreshingData() {
        Log.i(TAG, "Stopped refreshing data")
        handler.removeCallbacks(runnableCode)
    }

    fun getTramData() {
        tramCallInProgress = true
        scope.launch {
            val apiResponse = getAllVehicles(true)
            tramCallInProgress = false
            when (apiResponse) {
                is Result.Success -> apiResponse.data.result?.let { tramResults = it }
                is Result.Error -> errorMessage.postValue(apiResponse.exception.message)
            }
        }
    }

    fun getBusData() {
        busCallInProgress = true
        busScope.launch {
            val apiResponse = getAllVehicles(false)

            busCallInProgress = false
            when (apiResponse) {
                is Result.Success -> apiResponse.data.result?.let { busResults = it }
                is Result.Error -> errorMessage.postValue(apiResponse.exception.message)
            }
        }
    }

    private suspend fun getAllVehicles(trams: Boolean): Result<ApiResponse> {
        val type = if (trams) 2 else 1

        return safeApiResult(
            call = { warsawService.getAllVehicles(type).await() },
            errorMessage = "API call failure :("
        )
    }
}