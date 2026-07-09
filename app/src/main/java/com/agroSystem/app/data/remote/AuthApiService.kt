package com.agroSystem.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.DELETE
import com.agroSystem.app.data.models.Product

interface AuthApiService {
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): AuthResponse

    @POST("auth/phone")
    suspend fun loginWithPhone(@Body request: PhoneLoginRequest): AuthResponse

    @POST("auth/update-profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): AuthResponse

    @POST("payment/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): CheckoutResponse

    @GET("payment/orders/{userId}")
    suspend fun getUserOrders(@Path("userId") userId: String): OrdersListResponse

    @POST("products")
    suspend fun createProduct(@Body product: Product): CreateUpdateProductResponse

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: Product): CreateUpdateProductResponse

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): GeneralStatusResponse
}

data class CheckoutRequest(
    val userId: String,
    val amount: Int,
    val items: List<CheckoutItem>
)

data class CheckoutItem(
    val id: Int,
    val name: String,
    val price: Int,
    val quantity: Int
)

data class CheckoutResponse(
    val success: Boolean,
    val message: String?,
    val data: CheckoutData?
)

data class CheckoutData(
    val orderId: String?,
    val topupId: String?,
    val amount: Int,
    val status: String?,
    val payment: CheckoutPayment?
)

data class CheckoutPayment(
    val token: String?,
    val redirect_url: String?
)

data class GoogleLoginRequest(
    val idToken: String,
    val name: String,
    val email: String
)

data class PhoneLoginRequest(
    val phone: String,
    val name: String
)

data class UpdateProfileRequest(
    val userId: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val photoUrl: String?,
    val role: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val role: String,
    val photoUrl: String?,
    val address: String?
)

data class OrdersListResponse(
    val success: Boolean,
    val data: List<OrderItemResponse>?
)

data class OrderItemResponse(
    val orderId: String,
    val amount: Int,
    val status: String,
    val createdAt: String,
    val items: List<CheckoutItem>?,
    val payment: OrderPaymentInfo?
)

data class OrderPaymentInfo(
    val token: String?,
    val redirect_url: String?
)

data class CreateUpdateProductResponse(
    val success: Boolean,
    val data: Product?
)

data class GeneralStatusResponse(
    val success: Boolean,
    val message: String? = null
)
