package pl.com.andrzejgrzyb.tramwajelive.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import pl.com.andrzejgrzyb.tramwajelive.R
import pl.com.andrzejgrzyb.tramwajelive.adapter.VehicleAdapter
import pl.com.andrzejgrzyb.tramwajelive.model.VehicleInfo
import kotlinx.android.synthetic.main.fragment_vehicle_list.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class VehicleListFragment: Fragment() {

    private val vehicleListViewModel by sharedViewModel<VehicleDataViewModel>()

    companion object {
        //        val instance: VehicleListFragment by lazy { VehicleListFragment() }
        val instance: VehicleListFragment =
            VehicleListFragment()
    }


    private val recyclerViewAdapter = VehicleAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vehicle_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = recyclerViewAdapter
        vehicleListViewModel.vehicleData.observe(this, Observer { data ->
            Log.i("Fragment", "trams changed")
            recyclerViewAdapter.data = data
                .sortedWith(compareBy(VehicleInfo::lines, VehicleInfo::brigade))

        })
        vehicleListViewModel.toastMessage.observe(this, Observer {
            errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        })
    }
}