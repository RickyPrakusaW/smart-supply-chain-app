package com.agroSystem.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

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
