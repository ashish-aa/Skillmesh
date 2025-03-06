package com.pccoer.skillmesh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // State holders for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult: StateFlow<AuthResult?> = _authResult

    suspend fun checkUserProfileExists(userId: String): Boolean {
        return try {
            val document = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
            document.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false // Assume profile doesn't exist on error
        }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _authResult.value = AuthResult.Error("Invalid email or password must be at least 6 characters")
            return
        }

        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                _authResult.value = if (task.isSuccessful) {
                    AuthResult.Success
                } else {
                    AuthResult.Error(task.exception?.localizedMessage ?: "Unknown error")
                }
            }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authResult.value = AuthResult.Error("Please enter email and password")
            return
        }

        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                _authResult.value = if (task.isSuccessful) {
                    AuthResult.Success
                } else {
                    AuthResult.Error(task.exception?.localizedMessage ?: "Login failed")
                }
            }
    }
}

// Sealed class to handle authentication states
sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}
