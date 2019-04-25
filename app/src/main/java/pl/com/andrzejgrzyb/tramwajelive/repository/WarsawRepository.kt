package pl.com.andrzejgrzyb.tramwajelive.repository

import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import pl.com.andrzejgrzyb.tramwajelive.model.ApiResponse
import pl.com.andrzejgrzyb.tramwajelive.model.Result
import pl.com.andrzejgrzyb.tramwajelive.model.VehicleInfo
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext

class WarsawRepository(private val warsawService: WarsawService) : BaseRepository() {

    val TAG = "WarsawRepository"

    var tramResults: List<VehicleInfo> = emptyList()
        set(value) {
            field = value
            if (!busCallInProgress) {
                sumVehicleResults()
            }
        }

    private fun sumVehicleResults() {
        allVehicleResults.postValue(tramResults + busResults)
    }

    var busResults: List<VehicleInfo> = emptyList()
        set(value) {
            field = value
            if (!tramCallInProgress) {
                allVehicleResults.postValue(tramResults + busResults)
            }
        }

    var tramCallInProgress = false
    var busCallInProgress = false

    var allVehicleResults = MutableLiveData<List<VehicleInfo>>()

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
//            refreshData(true)
//            refreshData(false)
            handler.postDelayed(this, 5000)
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
            val apiResponse = safeApiResult(
                call = { warsawService.getAllVehicles2(1).await() },
                errorMessage = "Bus API call failure :("
            )
            busCallInProgress = false
            when (apiResponse) {
                is Result.Success -> apiResponse.data.result?.let {
                    Log.i(TAG, it[0].toString())
                    busResults = it
                }
                is Result.Error -> {
                    Log.i(TAG, apiResponse.exception.message)
                    errorMessage.postValue(apiResponse.exception.message)
                }
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