package pl.com.andrzejgrzyb.tramwajelive.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

class Vehicle(lat: Double, lon: Double, var time: String, val line: String, val brigade: String) {

    val id = "${line}_$brigade"
    var destinationLatLng = LatLng(lat, lon)
    var sourceLatLng: LatLng? = null
    var direction: Float? = null

    fun updateLocation(lat: Double, lon: Double, time: String) {
        sourceLatLng = destinationLatLng
        destinationLatLng = LatLng(lat, lon)
        this.time = time
        if (shouldMarkerBeRotated()) {
            direction = calculateDirection()
        }
    }

    private fun shouldMarkerBeRotated(): Boolean =
        SphericalUtil.computeDistanceBetween(destinationLatLng, sourceLatLng) > 10

    private fun calculateDirection(): Float =
        SphericalUtil.computeHeading(sourceLatLng, destinationLatLng).toFloat()
}
