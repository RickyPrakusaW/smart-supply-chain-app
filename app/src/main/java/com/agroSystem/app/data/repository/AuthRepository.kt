package com.agroSystem.app.data.repository

import android.util.Log
import com.agroSystem.app.data.local.dao.UserDao
import com.agroSystem.app.data.local.entities.toEntity
import com.agroSystem.app.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository(
    private val userDao: UserDao,
    private val context: android.content.Context
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private fun backupProfile(user: User) {
        val sp = context.getSharedPreferences("user_profile_backup", android.content.Context.MODE_PRIVATE)
        sp.edit().apply {
            putString("photo_${user.id}", user.photoUrl)
            putString("address_${user.id}", user.address)
            putString("phone_${user.id}", user.phone)
            apply()
        }
    }

    private fun restoreProfileBackup(user: User): User {
        val sp = context.getSharedPreferences("user_profile_backup", android.content.Context.MODE_PRIVATE)
        val savedPhoto = sp.getString("photo_${user.id}", null)
        val savedAddress = sp.getString("address_${user.id}", null)
        val savedPhone = sp.getString("phone_${user.id}", null)
        
        return user.copy(
            photoUrl = if (user.photoUrl.isNullOrEmpty()) savedPhoto else user.photoUrl,
            address = if (user.address.isNullOrEmpty()) savedAddress else user.address,
            phone = if (user.phone.isNullOrEmpty()) savedPhone else user.phone
        )
    }

    suspend fun loadSession(): User? = withContext(Dispatchers.IO) {
        val entity = userDao.getLoggedInUser()
        val user = entity?.toDomain()
        _currentUser.value = user
        return@withContext user
    }

    suspend fun loginWithGoogle(idToken: String, name: String, email: String): User = withContext(Dispatchers.IO) {
        val cleanEmail = email.replace(Regex("[^a-zA-Z0-9]"), "")
        val id = "google_$cleanEmail"
        var user: User? = null
        
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(id)
            val snapshot = suspendCancellableCoroutine<com.google.firebase.firestore.DocumentSnapshot?> { cont ->
                docRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) cont.resume(task.result)
                    else cont.resume(null)
                }
            }

            if (snapshot != null && snapshot.exists()) {
                user = User(
                    id = id,
                    name = snapshot.getString("name") ?: name,
                    email = snapshot.getString("email") ?: email,
                    phone = snapshot.getString("phone"),
                    role = snapshot.getString("role") ?: "Pembeli",
                    token = "jwt_mock_token_for_" + idToken.take(10),
                    photoUrl = snapshot.getString("photoUrl"),
                    address = snapshot.getString("address")
                )
            } else {
                val newUserMap = hashMapOf(
                    "id" to id,
                    "name" to name,
                    "email" to email,
                    "phone" to null,
                    "role" to "Pembeli",
                    "photoUrl" to null,
                    "address" to null
                )
                suspendCancellableCoroutine<Void?> { cont ->
                    docRef.set(newUserMap).addOnCompleteListener { task ->
                        cont.resume(null)
                    }
                }
                user = User(
                    id = id,
                    name = name,
                    email = email,
                    phone = null,
                    role = "Pembeli",
                    token = "jwt_mock_token_for_" + idToken.take(10),
                    photoUrl = null,
                    address = null
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Firestore Google Login failed, using local fallback", e)
        }

        if (user == null) {
            user = User(
                id = id,
                name = name,
                email = email,
                phone = null,
                role = "Pembeli",
                token = "jwt_mock_token_for_" + idToken.take(10),
                photoUrl = null,
                address = null
            )
        }

        user = restoreProfileBackup(user)
        backupProfile(user)

        userDao.clearUser()
        userDao.insertUser(user.toEntity())
        _currentUser.value = user
        return@withContext user
    }

    suspend fun loginWithPhone(phone: String, name: String): User = withContext(Dispatchers.IO) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val id = "phone_$cleanPhone"
        var user: User? = null
        
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(id)
            val snapshot = suspendCancellableCoroutine<com.google.firebase.firestore.DocumentSnapshot?> { cont ->
                docRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) cont.resume(task.result)
                    else cont.resume(null)
                }
            }

            if (snapshot != null && snapshot.exists()) {
                user = User(
                    id = id,
                    name = snapshot.getString("name") ?: (if (name.isNotEmpty()) name else "User Telpon"),
                    email = snapshot.getString("email"),
                    phone = snapshot.getString("phone") ?: phone,
                    role = snapshot.getString("role") ?: "Pembeli",
                    token = "jwt_mock_token_for_" + phone,
                    photoUrl = snapshot.getString("photoUrl"),
                    address = snapshot.getString("address")
                )
            } else {
                val displayName = if (name.isNotEmpty()) name else "User Telpon"
                val newUserMap = hashMapOf(
                    "id" to id,
                    "name" to displayName,
                    "email" to null,
                    "phone" to phone,
                    "role" to "Pembeli",
                    "photoUrl" to null,
                    "address" to null
                )
                suspendCancellableCoroutine<Void?> { cont ->
                    docRef.set(newUserMap).addOnCompleteListener { task ->
                        cont.resume(null)
                    }
                }
                user = User(
                    id = id,
                    name = displayName,
                    email = null,
                    phone = phone,
                    role = "Pembeli",
                    token = "jwt_mock_token_for_" + phone,
                    photoUrl = null,
                    address = null
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Firestore Phone Login failed, using local fallback", e)
        }

        if (user == null) {
            user = User(
                id = id,
                name = name.ifEmpty { "User Telpon" },
                email = null,
                phone = phone,
                role = "Pembeli",
                token = "jwt_mock_token_for_" + phone,
                photoUrl = null,
                address = null
            )
        }

        user = restoreProfileBackup(user)
        backupProfile(user)

        userDao.clearUser()
        userDao.insertUser(user.toEntity())
        _currentUser.value = user
        return@withContext user
    }

    suspend fun updateProfile(
        name: String,
        email: String?,
        phone: String?,
        address: String?,
        photoUrl: String?,
        role: String
    ): User? = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext null
        var updated: User? = null
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(current.id)
            
            val updateMap = hashMapOf<String, Any?>(
                "name" to name,
                "role" to role
            )
            if (email != null) updateMap["email"] = email
            if (phone != null) updateMap["phone"] = phone
            if (address != null) updateMap["address"] = address
            if (photoUrl != null) updateMap["photoUrl"] = photoUrl

            suspendCancellableCoroutine<Void?> { cont ->
                docRef.update(updateMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(null)
                    } else {
                        docRef.set(updateMap, com.google.firebase.firestore.SetOptions.merge()).addOnCompleteListener {
                            cont.resume(null)
                        }
                    }
                }
            }
            
            updated = User(
                id = current.id,
                name = name,
                email = email ?: current.email,
                phone = phone ?: current.phone,
                role = role,
                token = current.token,
                photoUrl = photoUrl ?: current.photoUrl,
                address = address ?: current.address
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Firestore Profile Update failed, using local fallback", e)
        }

        if (updated == null) {
            updated = current.copy(
                name = name,
                email = email ?: current.email,
                phone = phone ?: current.phone,
                address = address ?: current.address,
                photoUrl = photoUrl ?: current.photoUrl,
                role = role
            )
        }

        backupProfile(updated)

        userDao.insertUser(updated.toEntity())
        _currentUser.value = updated
        return@withContext updated
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.clearUser()
        _currentUser.value = null
    }
}
