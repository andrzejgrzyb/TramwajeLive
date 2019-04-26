package pl.com.andrzejgrzyb.tramwajelive.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import pl.com.andrzejgrzyb.tramwajelive.R
import pl.com.andrzejgrzyb.tramwajelive.model.VehicleInfo
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import pl.com.andrzejgrzyb.tramwajelive.MainViewModel
import java.lang.NumberFormatException

private const val TAG = "MapFragment"

class MapFragment : Fragment(), OnMapReadyCallback {

    private val MY_PERMISSIONS_REQUEST_LOCATION = 100

    private lateinit var mMapView: MapView
    private lateinit var mMap: GoogleMap

    private val mapViewModel by sharedViewModel<VehicleDataViewModel>()
    private val mainViewModel by sharedViewModel<MainViewModel>()

    companion object {
        //        val instance: VehicleListFragment by lazy { VehicleListFragment() }
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
        // we build google api client

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
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(9.0f))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 11.0f))
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        checkLocationPermissions()


        mapViewModel.vehicleData.observe(this, Observer { data ->
            Log.i(TAG, "trams changed")
            updateMapMarkers(data)

        })
        mainViewModel.filteredLineNumbers.observe(this, Observer {
            Log.i(TAG, "Filters changed: $it")
            updateMapMarkers(mapViewModel.vehicleData.value!!)
        })
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
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
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

    private fun updateMapMarkers(data: List<VehicleInfo>) {
        mMap.clear()

        data.filter {
            mainViewModel.filteredLineNumbers.value?.contains(it.lines) ?: true
        }
            .forEach {
                mMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            it.lat,
                            it.lon
                        )
                    ).title("Linia ${it.lines}, Brygada ${it.brigade}")
                )
            }
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
        Log.i("Weird lines", data.sortedBy { it.lines }
            .filter {
                try {
                    it.lines.toInt()
                    false
                } catch (e: NumberFormatException) {
                    true
                }
            }
            .joinToString(separator = ",", transform = { it.lines }))
        Toast.makeText(context, "Refreshed vehicles' positions", Toast.LENGTH_SHORT).show()
    }
}
