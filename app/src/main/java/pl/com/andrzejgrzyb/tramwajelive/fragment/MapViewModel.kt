package pl.com.andrzejgrzyb.tramwajelive.fragment

import pl.com.andrzejgrzyb.tramwajelive.BaseViewModel
import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawRepository

class MapViewModel(private val warsawRepository: WarsawRepository) : BaseViewModel() {

    var tramResults = warsawRepository.allVehicleResults

}