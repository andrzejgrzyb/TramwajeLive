package pl.com.andrzejgrzyb.tramwajelive.repository

import pl.com.andrzejgrzyb.tramwajelive.model.ApiResponse
import pl.com.andrzejgrzyb.tramwajelive.model.Result

class WarsawRepository(private val warsawService: WarsawService) : BaseRepository() {

    private val TAG = "WarsawRepository"

    suspend fun getAllVehicles(trams: Boolean): Result<ApiResponse> {
        val type = if (trams) 2 else 1
        return safeApiResult(
            call = { warsawService.getAllVehicles(type).await() },
            errorMessage = "API call failure :("
        )
    }
}