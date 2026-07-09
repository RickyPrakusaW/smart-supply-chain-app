package com.agroSystem.app

import com.agroSystem.app.data.local.dao.FarmerDao
import com.agroSystem.app.data.local.dao.ProductDao
import com.agroSystem.app.data.local.entities.ProductEntity
import com.agroSystem.app.data.local.entities.toEntity
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.remote.ApiService
import com.agroSystem.app.data.repository.AgriRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class AgriRepositoryTest {

    @Mock
    private lateinit var productDao: ProductDao

    @Mock
    private lateinit var farmerDao: FarmerDao

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: AgriRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = AgriRepository(productDao, farmerDao, apiService)
    }

    @Test
    fun getProducts_whenCacheEmpty_fetchesFromRemoteAndCaches() = runTest {
        // Arrange
        `when`(productDao.getAllProducts()).thenReturn(emptyList())
        val mockRemoteProducts = listOf(
            Product(
                id = 99,
                name = "Mock Product",
                farmer = "Mock Farmer",
                rating = "4.5",
                price = 10000,
                unit = "1 kg",
                imageResId = 0,
                category = "Sayuran"
            )
        )
        `when`(apiService.getProducts()).thenReturn(mockRemoteProducts)
        `when`(productDao.getAllProducts()).thenReturn(mockRemoteProducts.map { it.toEntity() })

        // Act
        val result = repository.getProducts(forceRefresh = true)

        // Assert
        verify(apiService).getProducts()
        verify(productDao).clearAllProducts()
        verify(productDao).insertProducts(mockRemoteProducts.map { it.toEntity() })
        assertEquals(1, result.size)
        assertEquals("Mock Product", result[0].name)
    }

    @Test
    fun getProducts_whenRemoteFails_fallsBackToCache() = runTest {
        // Arrange
        val mockCachedEntities = listOf(
            ProductEntity(
                id = 1,
                name = "Cached Product",
                farmer = "Cached Farmer",
                rating = "4.5",
                price = 12000,
                unit = "1 kg",
                imageResId = 0,
                category = "Sayuran",
                isDiscounted = false,
                originalPrice = 0,
                isEcoFriendly = true,
                deliveryDays = 1,
                protein = "1g",
                fat = "0g",
                carbs = "2g",
                calories = "10 Kcal",
                ingredients = "None",
                shelfLife = "3 Days",
                storage = "Cool",
                packaging = "Box",
                diets = emptyList(),
                allergens = emptyList(),
                nutrients = emptyList()
            )
        )
        `when`(productDao.getAllProducts()).thenReturn(mockCachedEntities)
        `when`(apiService.getProducts()).thenThrow(RuntimeException("Network failure"))

        // Act
        val result = repository.getProducts(forceRefresh = true)

        // Assert
        assertEquals(1, result.size)
        assertEquals("Cached Product", result[0].name)
    }
}
