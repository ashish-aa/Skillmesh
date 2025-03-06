package com.pccoer.skillmesh.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _profileResult = MutableStateFlow<ProfileResult?>(null)
    val profileResult: StateFlow<ProfileResult?> = _profileResult


    fun clearProfileResult() {
        _profileResult.value = null
    }

    fun createProfile(
        firstName: String,
        lastName: String,
        dateOfBirth: Long?,
        location: String?,
        imageUri: Uri?,
        onProfileSaved: () -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _profileResult.value = ProfileResult.Error("User not authenticated.")
            return
        }

        if (firstName.isBlank() || lastName.isBlank() || dateOfBirth == null) {
            _profileResult.value = ProfileResult.Error("Please fill in all required fields.")
            return
        }
        println("createProfile: imageUri: $imageUri") // Log the URI

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Upload profile image if provided
                val imageUrl = imageUri?.let { uploadImage(it, userId) }
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(dateOfBirth))


                // Prepare profile data
                val profileData = hashMapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "dateOfBirth" to formattedDate,
                    "location" to location,
                    "imageUrl" to (imageUrl ?: ""),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "profileCompleted" to true
                )

                // Save profile data to Firestore
                firestore.collection("users").document(userId).set(profileData).await()

                _profileResult.value = ProfileResult.Success
                onProfileSaved()  // Navigate to next screen after saving
            } catch (e: Exception) {
                _profileResult.value = ProfileResult.Error(e.localizedMessage ?: "An unexpected error occurred.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri, userId: String): String? {
        val storageRef = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")
        println("uploadImage: Starting upload for user $userId, imageUri: $imageUri") // Log start
        return try {
            val uploadTask = storageRef.putFile(imageUri).await()  // Ensure upload completes
            val downloadUrl = storageRef.downloadUrl.await().toString()
            println("uploadImage: Download URL: $downloadUrl") // Log URL
            downloadUrl // Get the actual download URL
        } catch (e: StorageException) {
            println("uploadImage: Storage error: ${e.localizedMessage}") // Log error
            _profileResult.value = ProfileResult.Error("Storage error: ${e.localizedMessage}")
            null
        } catch (e: IOException) {
            println("uploadImage: Network error: ${e.localizedMessage}") // Log error
            _profileResult.value = ProfileResult.Error("Network error: ${e.localizedMessage}")
            null
        } catch (e: Exception) {
            println("uploadImage: Unexpected error: ${e.localizedMessage}") // Log error
            _profileResult.value =
                ProfileResult.Error("An unexpected error occurred during image upload: ${e.localizedMessage}")
            null
        }
    }


}

// Sealed class to handle profile creation states
sealed class ProfileResult {
    object Success : ProfileResult()
    data class Error(val message: String) : ProfileResult()
}