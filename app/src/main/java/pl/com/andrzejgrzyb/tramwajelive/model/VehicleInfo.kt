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
    val line: String,
    @SerializedName("Brigade")
    val brigade: String,
    var direction: Float?
) {
    val markerKey: String
        get() = "${line}_$brigade"
}