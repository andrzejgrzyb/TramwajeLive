package pl.com.andrzejgrzyb.tramwajelive.model

import com.google.gson.annotations.SerializedName

data class VehicleInfo(
    @SerializedName("Lat")
    val lat: Double,
    @SerializedName("Lon")
    val lon: Double,
    @SerializedName("Time")
    val time: String,
    @SerializedName("Lines")
    val lines: String,
    @SerializedName("Brigade")
    val brigade: String
)