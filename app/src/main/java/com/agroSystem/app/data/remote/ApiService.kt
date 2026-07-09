package com.agroSystem.app.data.remote

import com.agroSystem.app.data.models.Farmer
import com.agroSystem.app.data.models.Product
import retrofit2.http.GET

interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>

    @GET("farmers")
    suspend fun getFarmers(): List<Farmer>
}
