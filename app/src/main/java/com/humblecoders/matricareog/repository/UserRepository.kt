package com.humblecoders.matricareog.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.humblecoders.matricareog.DataStoreManager
import com.humblecoders.matricareog.model.AuthResult
import com.humblecoders.matricareog.model.User
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

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
                dataStoreManager.saveUserSession(firebaseUser.uid, email, fullName)
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
                dataStoreManager.saveUserSession(firebaseUser.uid, user.email, user.fullName)
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

    suspend fun checkCurrentUser(): AuthResult {
        // The ONLY thing that determines if the user is logged in is auth.currentUser.
        // Never return Error just because a Firestore network call fails —
        // that would log out a perfectly valid user whenever their connection
        // is slow or momentarily drops (e.g. right after the OS restarts the process).
        val firebaseUser = awaitRestoredFirebaseUser()
        if (firebaseUser == null) {
            val session = dataStoreManager.getUserSession()
            if (session.isLoggedIn && !session.userId.isNullOrBlank()) {
                Log.w("UserRepository", "checkCurrentUser: Firebase session missing, restoring from DataStore")
                return AuthResult.Success(
                    User(
                        uid = session.userId,
                        email = session.email ?: "",
                        fullName = session.name ?: ""
                    )
                )
            }
            return AuthResult.Error("Not authenticated").also {
                Log.d("UserRepository", "checkCurrentUser: no Firebase session")
            }
        }

        return try {
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            val user = userDoc.toObject(User::class.java) ?: buildFallbackUser(firebaseUser)
            Log.d("UserRepository", "checkCurrentUser: found uid=${firebaseUser.uid}")
            AuthResult.Success(user)
        } catch (e: Exception) {
            // Firestore unavailable (no network, timeout, etc.) — keep the user logged in.
            Log.w("UserRepository", "Firestore unavailable on session restore, using fallback: ${e.message}")
            AuthResult.Success(buildFallbackUser(firebaseUser))
        }
    }

    private suspend fun awaitRestoredFirebaseUser(timeoutMs: Long = 3000L): FirebaseUser? {
        auth.currentUser?.let { return it }

        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                lateinit var listener: FirebaseAuth.AuthStateListener
                listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    if (user != null && cont.isActive) {
                        auth.removeAuthStateListener(listener)
                        cont.resume(user)
                    }
                }

                auth.addAuthStateListener(listener)

                cont.invokeOnCancellation {
                    auth.removeAuthStateListener(listener)
                }
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
            dataStoreManager.clearUserSession()
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
