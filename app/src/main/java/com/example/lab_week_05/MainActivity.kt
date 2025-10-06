package com.example.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_week_05.api.CatApiService
import com.example.lab_week_05.model.ImageData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    private val retrofit by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    private lateinit var apiResponseView: TextView
    private lateinit var imageResultView: ImageView
    private val imageLoader: ImageLoader by lazy { GlideLoader(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiResponseView = findViewById(R.id.api_response)
        imageResultView = findViewById(R.id.image_result)

        getCatImageResponse()
    }

    private fun getCatImageResponse() {
        val call = catApiService.searchImages(1, "full")

        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "❌ Failed to get response", t)
                apiResponseView.text = "Failed to get response: ${t.message}"
            }

            override fun onResponse(
                call: Call<List<ImageData>>,
                response: Response<List<ImageData>>
            ) {
                if (response.isSuccessful) {
                    val imageList = response.body()
                    Log.d(MAIN_ACTIVITY, "✅ Response received: $imageList")

                    val firstImage = imageList?.firstOrNull()?.imageUrl.orEmpty()
                    Log.d(MAIN_ACTIVITY, "🖼️ firstImage URL: $firstImage")

                    if (firstImage.isNotBlank()) {
                        imageLoader.loadImage(firstImage, imageResultView)
                        apiResponseView.text =
                            getString(R.string.image_placeholder, firstImage)
                    } else {
                        Log.e(MAIN_ACTIVITY, "⚠️ No image URL found in API response")
                        apiResponseView.text = "No image found."
                    }
                } else {
                    Log.e(
                        MAIN_ACTIVITY,
                        "❌ API response failed: ${response.errorBody()?.string().orEmpty()}"
                    )
                    apiResponseView.text = "Failed to load image data."
                }
            }
        })
    }

    companion object {
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
    }
}
