package pl.com.andrzejgrzyb.tramwajelive.repository

import pl.com.andrzejgrzyb.tramwajelive.model.ApiResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WarsawService {

    @GET("api/action/busestrams_get/")
    fun getAllVehicles(@Query("type") type: Int): Deferred<Response<ApiResponse>>

    @GET("api/action/busestrams_get/")
    fun getAllVehicles2(@Query("type") type: Int): Deferred<Response<ApiResponse>>
//    fun getAllVehicles(@Query("type") type: Int): Call<ApiResponse>
}