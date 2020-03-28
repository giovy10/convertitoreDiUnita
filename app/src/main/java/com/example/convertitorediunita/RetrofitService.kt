package com.example.convertitorediunita

import android.util.Log
import com.example.convertitorediunita.ui.converter.MoneyUtilResult
import com.example.convertitorediunita.ui.model.Money
import com.example.convertitorediunita.ui.model.MoneyUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val API_KEY = "35160723af91450bb52cb10b2a3c88ba"

interface MoneyUtilResultReceiver {
    fun receive(result: MoneyUtilResult)
}

class RetrofiteService {
    //the okhttp Interceptor, need to view in the logCat the response of the server
    //in json file

    val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://openexchangerates.org/api/")
        .client(client)
        .build()

    private val service: RetrofitServiceInterface =
        retrofit.create<RetrofitServiceInterface>(RetrofitServiceInterface::class.java)

    fun getDataFromApi(receveir: MoneyUtilResultReceiver) {
        service
            .getEuroMoney(API_KEY, "USD")
            .enqueue(object : Callback<Money> {
                override fun onResponse(call: Call<Money>, response: Response<Money>) {
                    val success = response.body()
                    if (success != null) {
                        val moneyUtil = success.toMoneyUtil()
                        receveir.receive(MoneyUtilResult.Success(moneyUtil))
                        Log.d("OnSuccess", success.toString())
                    } else {
                        receveir.receive(
                            MoneyUtilResult.Error(
                                Throwable(
                                    response.errorBody().toString()
                                )
                            )
                        )
                    }
                }

                override fun onFailure(call: Call<Money>, t: Throwable) {
                    Log.d("OnFailure", "error: $t")
                    receveir.receive(MoneyUtilResult.Error(Throwable("error")))
                }
            })
    }

    suspend fun moneyDetail(): MoneyUtil {
        val response = service.getEuroMoneyDetail(app_id = API_KEY, base = "USD")
        val success = response.body()

        if (success != null) {
            return success.toMoneyUtil()
        } else {
            throw Exception(Throwable(response.errorBody().toString()))
        }
    }
}

interface RetrofitServiceInterface {
    @GET("latest.json")
    fun getEuroMoney(@Query(value = "app_id") app_id: String, @Query("base") base: String): Call<Money>

    @GET("latest.json?{app_id}")
    suspend fun getEuroMoneyDetail(@Path("app_id") app_id: String, @Query("base") base: String): Response<Money>
}
