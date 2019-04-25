package pl.com.andrzejgrzyb.tramwajelive.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import pl.com.andrzejgrzyb.tramwajelive.model.Result
import retrofit2.Response
import java.io.IOException
import java.lang.Exception

open class BaseRepository {

    val errorMessage = MutableLiveData<String>()

    suspend fun <T : Any> safeApiCall(
        call: suspend () -> Response<T>,
        errorMessage: String
    ): T? {
        val result: Result<T> = safeApiResult(call, errorMessage)
        var data: T? = null

        when (result) {
            is Result.Success ->
                data = result.data
            is Result.Error -> {
                Log.d("1.DataRepository", "$errorMessage & Exception - ${result.exception}")
            }
        }
        return data
    }

    protected suspend fun <T : Any> safeApiResult(call: suspend () -> Response<T>, errorMessage: String): Result<T> {
        return try {
            val response = call.invoke()
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(IOException("Error Occurred during getting safe Api result, Custom ERROR - $errorMessage"))
            }
        } catch (e: Exception) {
            Result.Error(IOException(errorMessage))
        }
    }
}