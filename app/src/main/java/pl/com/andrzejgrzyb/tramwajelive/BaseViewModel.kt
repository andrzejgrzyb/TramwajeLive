package pl.com.andrzejgrzyb.tramwajelive

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {

    var toastMessage = MutableLiveData<String>()
}