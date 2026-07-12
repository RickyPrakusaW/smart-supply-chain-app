package com.agroSystem.app

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.repository.AgriRepository
import com.agroSystem.app.features.shared.MainSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class MainSharedViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var repository: AgriRepository

    private lateinit var viewModel: MainSharedViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Mock repository default values
        `when`(repository.defaultProducts).thenReturn(emptyList())
        `when`(repository.defaultFarmers).thenReturn(emptyList())

        viewModel = MainSharedViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addProductToCart_increasesQuantityCorrectly() = runTest {
        // Arrange
        val product = Product(
            id = 1,
            name = "Organic Spinach",
            farmer = "Tani Jaya",
            rating = "4.8",
            price = 15000,
            unit = "250g",
            imageResId = 0,
            category = "Sayuran"
        )

        // Act
        viewModel.addProductToCart(product)

        // Assert
        val cart = viewModel.cartItems.value
        assertTrue(cart != null)
        assertEquals(1, cart!![product])

        // Act again
        viewModel.addProductToCart(product)

        // Assert again
        assertEquals(2, cart[product])
    }

    @Test
    fun getFilteredProducts_filtersByNameCorrectly() = runTest {
        // Act
        viewModel.searchQuery.value = "Bayam"
        val result = viewModel.getFilteredProducts()

        // Assert
        assertEquals(1, result.size)
        assertEquals("Bayam Hidroponik Bersih", result[0].name)
    }
}
