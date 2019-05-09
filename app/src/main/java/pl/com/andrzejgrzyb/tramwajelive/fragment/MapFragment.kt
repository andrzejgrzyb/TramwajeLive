package pl.com.andrzejgrzyb.tramwajelive.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import pl.com.andrzejgrzyb.tramwajelive.MainViewModel
import pl.com.andrzejgrzyb.tramwajelive.R
import pl.com.andrzejgrzyb.tramwajelive.model.Vehicle
import pl.com.andrzejgrzyb.tramwajelive.model.VehicleInfo


private const val TAG = "MapFragment"

class MapFragment : Fragment(), OnMapReadyCallback {

    private val MY_PERMISSIONS_REQUEST_LOCATION = 100

    private lateinit var mMapView: MapView
    private lateinit var mMap: GoogleMap
    private val markersMap = HashMap<String, Marker>()

    private val mapViewModel by sharedViewModel<VehicleDataViewModel>()
    private val mainViewModel by sharedViewModel<MainViewModel>()

    private val vehicleMapObserver = Observer<HashMap<String, Vehicle>> { data ->
        Log.i(TAG, "map changed")
        updateMapMarkers(data)
    }
    private val filteredLineNumbersObserver = Observer<HashSet<String>> {
        Log.i(TAG, "Filters changed: $it")
        updateMapMarkers(mapViewModel.vehicleMap.value)
    }
    private val onCameraIdleListener = GoogleMap.OnCameraIdleListener {
        mapViewModel.cameraPosition = mMap.cameraPosition
        updateMapMarkers(mapViewModel.vehicleMap.value)
    }

    companion object {
        val instance: MapFragment by lazy { MapFragment() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        mMapView = rootView.findViewById(R.id.map)
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()

        try {
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mMapView.getMapAsync(this)

        return rootView
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val warsaw = LatLng(52.1974754, 21.0242043)
        if (mapViewModel.cameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mapViewModel.cameraPosition))
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 11.0f))
        }
        mMap.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = false
            isZoomControlsEnabled = true
            isRotateGesturesEnabled = false
        }
        mMap.setOnMarkerClickListener { it.showInfoWindow(); true }
        mMap.setOnCameraIdleListener(onCameraIdleListener)
        checkLocationPermissions()

        markersMap.clear()
        updateMapMarkers(mapViewModel.vehicleMap.value)
        mapViewModel.vehicleMap.observe(this, vehicleMapObserver)
        mainViewModel.filteredLineNumbers.observe(this, filteredLineNumbersObserver)
    }

    private fun checkLocationPermissions() {
        // Here, thisActivity is the current activity
        activity?.let {
            if (ContextCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionNotGranted()
            } else {
                enableMyLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        if (mapViewModel.cameraPosition == null) {
            val fusedLocationClient = activity?.let { LocationServices.getFusedLocationProviderClient(it) }
            fusedLocationClient?.lastLocation?.addOnSuccessListener {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0f))
            }
        }
    }

    private fun locationPermissionNotGranted() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)) {
//        } else {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
//        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    enableMyLocation()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            else -> {
            }
        }
    }

    private fun updateMapMarkers(data: HashMap<String, Vehicle>?) {
        if (data.isNullOrEmpty()) {
            return
        }
        val filteredData = mainViewModel.filteredLineNumbers.value.let { filtersSet ->
            if (filtersSet.isNullOrEmpty()) {
                data
            } else {
                data.filter { filtersSet.contains(it.value.line) }
            }
        }
        val latLngBounds = mMap.projection.visibleRegion.latLngBounds
        val keySet = filteredData.keys + markersMap.keys
        keySet.forEach { key ->
            if (filteredData.contains(key) && filteredData[key]?.let {
                    isVehicleWithinBounds(latLngBounds, it)
                } == true) {
                filteredData.getValue(key).let { vehicle ->
                    if (markersMap.containsKey(vehicle.id)) {
                        vehicle.direction?.let { direction ->
                            rotateMarker(markersMap[key]!!, vehicle.line, direction)
                        }
                        animateMarker(markersMap[key]!!, vehicle.destinationLatLng)
                    } else {
                        markersMap[vehicle.id] = drawMarker(vehicle)
                    }
                }
            } else {
                markersMap.remove(key)?.remove()
            }
        }
    }

    private fun isVehicleWithinBounds(latLngBounds: LatLngBounds, vehicle: Vehicle) =
        (latLngBounds.contains(vehicle.destinationLatLng)
                || vehicle.sourceLatLng?.let { latLngBounds.contains(it) } == true)

    private fun drawMarker(vehicle: Vehicle): Marker {
        return mMap.addMarker(
            MarkerOptions()
                .position(vehicle.destinationLatLng)
                .title("Line ${vehicle.line}, Brigade ${vehicle.brigade}")
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(vehicle.line, vehicle.direction)))
                .flat(true)
        )
    }

    private fun rotateMarker(marker: Marker, line: String, direction: Float) =
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(line, direction)))

    private fun getMarkerBitmapFromView(name: String, direction: Float?): Bitmap {
        val customMarkerView =
            (context!!.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.map_marker,
                null
            )
        return customMarkerView.run {
            findViewById<TextView>(R.id.txt_name).text = name
            findViewById<ImageView>(R.id.marker_arrow).apply {
                if (direction != null) {
                    rotation = direction
                    visibility = View.VISIBLE
                } else {
                    visibility = View.INVISIBLE
                }
            }
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            layout(0, 0, measuredWidth, measuredHeight)
            val returnedBitmap = Bitmap.createBitmap(
                measuredWidth, measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            draw(Canvas(returnedBitmap))
            returnedBitmap
        }
    }

    private fun animateMarker(marker: Marker, toPosition: LatLng) {
        val handler = Handler()
        val startTime = SystemClock.uptimeMillis()
        val startLatLng = marker.position
        val duration: Long = 500
        val interpolator = LinearInterpolator()

        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                val lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude
                val lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude
                marker.position = LatLng(lat, lng)

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    private fun variuosLogs(data: List<VehicleInfo>) {
        Log.i("Weird brigades", data.sortedBy { it.brigade }
            .filter {
                try {
                    it.brigade.toInt()
                    false
                } catch (e: NumberFormatException) {
                    true
                }
            }
            .joinToString(separator = ",", transform = { it.brigade }))
        Log.i("Weird lines", data.sortedBy { it.line }
            .filter {
                try {
                    it.line.toInt()
                    false
                } catch (e: NumberFormatException) {
                    true
                }
            }
            .joinToString(separator = ",", transform = { it.line }))
    }
}
