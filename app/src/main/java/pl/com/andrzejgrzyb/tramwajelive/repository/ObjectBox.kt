package pl.com.andrzejgrzyb.tramwajelive.repository

import android.content.Context
import io.objectbox.BoxStore
import pl.com.andrzejgrzyb.tramwajelive.model.MyObjectBox

object ObjectBox {

    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context): BoxStore {
        if (::boxStore.isInitialized && !boxStore.isClosed) {
            return boxStore
        }

        boxStore = MyObjectBox.builder().androidContext(context.applicationContext).build()
        return boxStore
    }
}