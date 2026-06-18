package com.humblecoders.matricareog.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.humblecoders.matricareog.DataStoreManager
import com.humblecoders.matricareog.model.AuthResult
import com.humblecoders.matricareog.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dataStoreManager: DataStoreManager
) {

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        age: String
    ): AuthResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = User(
                    fullName = fullName,
                    email = email,
                    uid = firebaseUser.uid,
                    age = age
                )

                try {
                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .set(user)
                        .await()
                } catch (e: Exception) {
                    Log.w("UserRepository", "Firestore save failed during sign up", e)
                }

                Log.d("UserRepository", "User created successfully: $user")
                dataStoreManager.saveLoggedInUserId(firebaseUser.uid)
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Failed to create user account")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Sign up error: ${e.message}", e)
            AuthResult.Error(getErrorMessage(e))
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = fetchUserFromFirestore(firebaseUser) ?: buildFallbackUser(firebaseUser)
                dataStoreManager.saveLoggedInUserId(firebaseUser.uid)
                Log.d("UserRepository", "Login successful: $user")
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Login failed")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Login error: ${e.message}", e)
            AuthResult.Error(getErrorMessage(e))
        }
    }

    private suspend fun awaitFirebaseUser(): FirebaseUser? {
        auth.currentUser?.let { return it }

        val expectSession = !dataStoreManager.getLoggedInUserId().isNullOrBlank()
        if (!expectSession) {
            return null
        }

        repeat(10) { attempt ->
            delay(300L * (attempt + 1) / 2)
            auth.currentUser?.let { return it }
        }
        return auth.currentUser
    }

    suspend fun checkCurrentUser(): AuthResult {
        return try {
            val firebaseUser = awaitFirebaseUser()
            if (firebaseUser == null) {
                dataStoreManager.clearLoggedInUserId()
                return AuthResult.Error("Not authenticated")
            }

            val user = fetchUserFromFirestore(firebaseUser) ?: buildFallbackUser(firebaseUser)
            dataStoreManager.saveLoggedInUserId(firebaseUser.uid)
            Log.d("UserRepository", "Current user found: $user")
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Check current user error: ${e.message}", e)
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                dataStoreManager.saveLoggedInUserId(firebaseUser.uid)
                AuthResult.Success(buildFallbackUser(firebaseUser))
            } else {
                dataStoreManager.clearLoggedInUserId()
                AuthResult.Error("Not authenticated")
            }
        }
    }

    private suspend fun fetchUserFromFirestore(firebaseUser: FirebaseUser): User? {
        return try {
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            userDoc.toObject(User::class.java)?.copy(
                uid = firebaseUser.uid,
                email = userDoc.toObject(User::class.java)?.email?.ifBlank { firebaseUser.email ?: "" }
                    ?: firebaseUser.email ?: ""
            )
        } catch (e: Exception) {
            Log.w("UserRepository", "Firestore fetch failed, using Firebase Auth fallback", e)
            null
        }
    }

    private fun buildFallbackUser(firebaseUser: FirebaseUser): User {
        return User(
            fullName = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            uid = firebaseUser.uid,
            age = ""
        )
    }

    suspend fun logout(): AuthResult {
        return try {
            auth.signOut()
            dataStoreManager.clearLoggedInUserId()
            Log.d("UserRepository", "User logged out successfully")
            AuthResult.Error("Not authenticated")
        } catch (e: Exception) {
            Log.e("UserRepository", "Logout error: ${e.message}", e)
            AuthResult.Error("Logout failed")
        }
    }

    suspend fun updateUser(user: User): AuthResult {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Log.d("UserRepository", "User updated successfully: $user")
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Update error: ${e.message}", e)
            AuthResult.Error("Failed to update user: ${e.message}")
        }
    }

    suspend fun deleteAccount(): AuthResult {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                try {
                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .delete()
                        .await()

                    val medicalHistoryDocs = firestore.collection("medical_history")
                        .whereEqualTo("userId", firebaseUser.uid)
                        .get()
                        .await()

                    for (doc in medicalHistoryDocs.documents) {
                        doc.reference.delete().await()
                    }
                } catch (e: Exception) {
                    Log.w("UserRepository", "Firestore cleanup failed during delete", e)
                }

                firebaseUser.delete().await()
                dataStoreManager.clearAllData()

                Log.d("UserRepository", "User account deleted successfully")
                AuthResult.Error("Account deleted")
            } else {
                AuthResult.Error("No user to delete")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Delete account error: ${e.message}", e)
            AuthResult.Error("Failed to delete account: ${e.message}")
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("network error", true) == true ->
                "Network error. Please check your connection."
            exception.message?.contains("email", true) == true ->
                "Invalid email address format."
            exception.message?.contains("password", true) == true ->
                "Password should be at least 6 characters."
            exception.message?.contains("user-not-found", true) == true ->
                "No account found with this email."
            exception.message?.contains("wrong-password", true) == true ->
                "Incorrect password."
            exception.message?.contains("email-already-in-use", true) == true ->
                "An account with this email already exists."
            else -> exception.message ?: "An unexpected error occurred."
        }
    }
}
