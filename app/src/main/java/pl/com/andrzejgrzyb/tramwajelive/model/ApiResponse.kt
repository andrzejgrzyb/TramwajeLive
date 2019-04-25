package pl.com.andrzejgrzyb.tramwajelive.model

import com.google.gson.annotations.SerializedName

class ApiResponse {

    @SerializedName("result")
    var result: List<VehicleInfo>? = null
}