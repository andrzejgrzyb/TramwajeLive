package pl.com.andrzejgrzyb.tramwajelive.fragment

import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.CameraPosition
import pl.com.andrzejgrzyb.tramwajelive.BaseViewModel
import pl.com.andrzejgrzyb.tramwajelive.model.Vehicle
import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawRepository
import pl.com.andrzejgrzyb.tramwajelive.usecase.GetVehicleDataUseCase

const val API_CALL_INTERVAL = 10000L

class VehicleDataViewModel(
    warsawRepository: WarsawRepository, private val getVehicleDataUseCase: GetVehicleDataUseCase
) : BaseViewModel() {

    companion object {
        private const val TAG = "VehicleDataViewModel"
    }

    private val handler = Handler()
    val vehicleMap: LiveData<MutableMap<String, Vehicle>>

    init {
        toastMessage = warsawRepository.errorMessage
        vehicleMap = getVehicleDataUseCase.vehiclePositions
    }

    var cameraPosition: CameraPosition? = null
    val lineNumbers: List<String>
        get() {
            return getVehicleDataUseCase.lineNumbers
        }

    private val runnableCode = object : Runnable {
        override fun run() {
            Log.i(TAG, "in runnable")
            // Do something here on the main thread
            getVehicleDataUseCase.refreshVehiclePositions()
            handler.postDelayed(this, API_CALL_INTERVAL)
        }
    }

    fun startRefreshingVehiclePositions() {
        handler.removeCallbacks(runnableCode)
        handler.post(runnableCode)
    }

    fun stopRefreshingVehiclePositions() {
        Log.i(TAG, "Stopped refreshing data")
        handler.removeCallbacks(runnableCode)
    }
}