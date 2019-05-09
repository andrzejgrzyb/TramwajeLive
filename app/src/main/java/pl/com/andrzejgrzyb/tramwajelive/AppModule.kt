package pl.com.andrzejgrzyb.tramwajelive

import pl.com.andrzejgrzyb.tramwajelive.fragment.VehicleDataViewModel
import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawRepository
import pl.com.andrzejgrzyb.tramwajelive.repository.WarsawService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.com.andrzejgrzyb.tramwajelive.fragment.FilterViewModel
import pl.com.andrzejgrzyb.tramwajelive.repository.FilterRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val networkModule = module {
    single { provideWarsawService() }
    single { WarsawRepository(get()) }
}

val localRepository = module {
    single { FilterRepository() }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get()) }
    viewModel { VehicleDataViewModel(get()) }
    viewModel { FilterViewModel(get()) }
}

fun provideWarsawService(): WarsawService {
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

    val apiParamsInterceptor = Interceptor {
        var request = it.request()
        val url = request.url().newBuilder()
            .addQueryParameter("resource_id",
                BuildConfig.UM_WARSAW_VEHICLES_RESOURCE_ID
            )
            .addQueryParameter("apikey", BuildConfig.UM_WARSAW_API_KEY)
            .build()
        request = request.newBuilder().url(url).build()
        return@Interceptor it.proceed(request)
    }

    val client = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor)
        .addInterceptor(apiParamsInterceptor)
        .build()

    return Retrofit.Builder()
        .baseUrl(BuildConfig.UM_WARSAW_API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
        .create(WarsawService::class.java)
}

