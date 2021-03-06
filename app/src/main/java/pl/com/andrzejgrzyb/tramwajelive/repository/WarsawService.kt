package pl.com.andrzejgrzyb.tramwajelive.repository

import kotlinx.coroutines.Deferred
import pl.com.andrzejgrzyb.tramwajelive.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface WarsawService {

    @Headers("Cache-Control: no-cache")
    @GET("api/action/busestrams_get/")
    fun getAllVehicles(@Query("type") type: Int): Deferred<Response<ApiResponse>>
}