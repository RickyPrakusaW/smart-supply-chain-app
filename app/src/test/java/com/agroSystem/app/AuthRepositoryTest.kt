package com.agroSystem.app

import com.agroSystem.app.data.local.dao.UserDao
import com.agroSystem.app.data.local.entities.UserEntity
import com.agroSystem.app.data.local.entities.toEntity
import com.agroSystem.app.data.models.User
import com.agroSystem.app.data.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

import com.agroSystem.app.data.remote.AuthApiService

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var authApiService: AuthApiService

    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepository(userDao, authApiService)
    }

    @Test
    fun loadSession_whenUserExists_loadsIntoStateFlow() = runTest {
        // Arrange
        val cachedEntity = UserEntity(
            id = "google_12345",
            name = "Ricky Prakusa",
            email = "ricky@gmail.com",
            phone = null,
            role = "Pembeli",
            token = "mock_token",
            photoUrl = null
        )
        `when`(userDao.getLoggedInUser()).thenReturn(cachedEntity)

        // Act
        val result = authRepository.loadSession()

        // Assert
        verify(userDao).getLoggedInUser()
        assertEquals("Ricky Prakusa", result?.name)
        assertEquals("Ricky Prakusa", authRepository.currentUser.value?.name)
    }

    @Test
    fun loginWithGoogle_clearsOldAndSavesNewSession() = runTest {
        // Act
        val result = authRepository.loginWithGoogle(
            idToken = "google_id_token",
            name = "Ricky Google",
            email = "ricky.google@gmail.com"
        )

        // Assert
        verify(userDao).clearUser()
        verify(userDao).insertUser(result.toEntity())
        assertEquals("Ricky Google", authRepository.currentUser.value?.name)
        assertEquals("ricky.google@gmail.com", authRepository.currentUser.value?.email)
    }

    @Test
    fun loginWithPhone_clearsOldAndSavesNewSession() = runTest {
        // Act
        val result = authRepository.loginWithPhone(
            phone = "8123456789",
            name = "Ricky Phone"
        )

        // Assert
        verify(userDao).clearUser()
        verify(userDao).insertUser(result.toEntity())
        assertEquals("8123456789", authRepository.currentUser.value?.phone)
        assertEquals("Pembeli", authRepository.currentUser.value?.role)
    }

    @Test
    fun logout_clearsDatabaseAndResetsState() = runTest {
        // Act
        authRepository.logout()

        // Assert
        verify(userDao).clearUser()
        assertNull(authRepository.currentUser.value)
    }
}
