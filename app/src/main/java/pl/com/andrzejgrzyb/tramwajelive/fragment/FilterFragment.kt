package pl.com.andrzejgrzyb.tramwajelive.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pl.com.andrzejgrzyb.tramwajelive.R
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_filter.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class FilterFragment : Fragment() {

    private val vehicleDataViewModel by sharedViewModel<VehicleDataViewModel>()

    companion object {
        val instance: FilterFragment by lazy { FilterFragment() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_filter, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        vehicleDataViewModel.lineNumbers.forEach {
            val chip = Chip(context)
            chip.isCheckable = true
            chip.checkedIcon = null
            chip.text = it
            lines_chip_group.addView(chip)
        }
    }
}