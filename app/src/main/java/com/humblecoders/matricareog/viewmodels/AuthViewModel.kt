package com.humblecoders.matricareog.viewmodels



import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.humblecoders.matricareog.model.AuthResult
import com.humblecoders.matricareog.model.User
import com.humblecoders.matricareog.repository.UserRepository
import com.humblecoders.matricareog.util.PregnancyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authState: StateFlow<AuthResult> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /** True after the first auth/session check finishes (success or failure). */
    private val _sessionChecked = MutableStateFlow(false)
    val sessionChecked: StateFlow<Boolean> = _sessionChecked.asStateFlow()

    // Form validation states
    private val _isTermsAccepted = MutableStateFlow(false)
    val isTermsAccepted: StateFlow<Boolean> = _isTermsAccepted.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                _currentUser.value = null
            } else if (_currentUser.value?.uid != firebaseUser.uid) {
                viewModelScope.launch {
                    val result = userRepository.checkCurrentUser()
                    if (result is AuthResult.Success) {
                        _currentUser.value = result.user
                        _authState.value = result
                    }
                }
            }
        }
        checkAuthState()
    }

    fun signUp(email: String, password: String, confirmPassword: String, fullName: String, age: String) {
        val validationError = validateSignUpInputs(email, password, confirmPassword, fullName)
        if (validationError != null) {
            _authState.value = AuthResult.Error(validationError)
            return
        }


        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            Log.d("UserViewModel", "$email, $password, $fullName, $age")
            val result = userRepository.signUp(email, password, fullName, age)
            _authState.value = result

            if (result is AuthResult.Success) {
                _currentUser.value = result.user
            }
        }
    }

    fun login(email: String, password: String) {
        val validationError = validateLoginInputs(email, password)
        if (validationError != null) {
            _authState.value = AuthResult.Error(validationError)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            val result = userRepository.login(email, password)
            _authState.value = result

            if (result is AuthResult.Success) {
                _currentUser.value = result.user
            }
        }
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            val result = userRepository.updateUser(user)
            if (result is AuthResult.Success) {
                _currentUser.value = result.user
            }
        }
    }

    fun clearAuthState() {
        _authState.value = AuthResult.Idle
    }

    fun setTermsAccepted(accepted: Boolean) {
        _isTermsAccepted.value = accepted
    }

    private fun validateSignUpInputs(
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String
    ): String? {
        return when {
            fullName.isBlank() -> "Please enter your full name"
            fullName.length < 2 -> "Full name must be at least 2 characters"
            fullName.length > 50 -> "Full name must be less than 50 characters"
            !fullName.matches(Regex("^[a-zA-Z\\s]+$")) -> "Full name can only contain letters and spaces"
            
            email.isBlank() -> "Please enter your email"
            !isValidEmail(email) -> "Please enter a valid email address"
            email.length > 100 -> "Email address is too long"
            
            password.isBlank() -> "Please create a password"
            password.length < 8 -> "Password must be at least 8 characters"
            password.length > 128 -> "Password must be less than 128 characters"
            !password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")) -> 
                "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
            
            confirmPassword.isBlank() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            
            else -> null
        }
    }

    private fun validateLoginInputs(email: String, password: String): String? {
        return when {
            email.isBlank() -> "Please enter your email"
            !isValidEmail(email) -> "Please enter a valid email address"
            password.isBlank() -> "Please enter your password"
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
               email.contains("@") && 
               email.contains(".") &&
               email.indexOf("@") < email.lastIndexOf(".")
    }

    fun checkAuthState() {
        viewModelScope.launch {
            _sessionChecked.value = false
            try {
                val result = userRepository.checkCurrentUser()
                _authState.value = result

                when (result) {
                    is AuthResult.Success -> _currentUser.value = result.user
                    is AuthResult.Error -> {
                        if (result.message == "Not authenticated") {
                            _currentUser.value = null
                            Log.w("AuthViewModel", "Auth check failed: ${result.message}")
                        } else {
                            Log.w("AuthViewModel", "Auth check warning: ${result.message}")
                        }
                    }
                    is AuthResult.Loading -> Unit
                    is AuthResult.Idle -> _currentUser.value = null
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Auth state check failed", e)
                _authState.value = AuthResult.Error("Authentication check failed")
            } finally {
                _sessionChecked.value = true
            }
        }
    }

    /** Syncs Firestore weeksPregnant when due date implies a new week (e.g. after reopening the app). */
    fun syncPregnancyWeeksIfNeeded() {
        val user = _currentUser.value ?: return
        if (!PregnancyUtils.hasDueDate(user.dueDate)) return

        val liveWeeks = PregnancyUtils.currentWeeksPregnant(user.dueDate, user.weeksPregnant)
        if (liveWeeks <= 0) return

        val storedWeeks = user.weeksPregnant.toIntOrNull() ?: 0
        if (liveWeeks != storedWeeks) {
            updateUserProfile(user.copy(weeksPregnant = liveWeeks.toString()))
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val result = userRepository.logout()
                _authState.value = result
                _currentUser.value = null
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout failed", e)
                _authState.value = AuthResult.Error("Logout failed: ${e.message}")
                _currentUser.value = null
            }
        }
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _authState.value = AuthResult.Loading
                val result = userRepository.deleteAccount()
                _authState.value = result
                _currentUser.value = null
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Delete account failed", e)
                _authState.value = AuthResult.Error("Delete account failed: ${e.message}")
            }
        }
    }

}
