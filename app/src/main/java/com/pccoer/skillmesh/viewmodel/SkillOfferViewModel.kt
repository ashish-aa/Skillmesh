package com.pccoer.skillmesh.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pccoer.skillmesh.data.SkillOffer
import kotlinx.coroutines.launch

class SkillOfferViewModel : ViewModel() {
    val skillOffer = mutableStateOf(SkillOffer())
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun updateSkillOffer(newSkillOffer: SkillOffer) {
        skillOffer.value = newSkillOffer
    }

    fun submitSkillOffer(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentOffer = skillOffer.value
        val currentUser = auth.currentUser

        if (currentOffer.title.isBlank() || currentOffer.category.isBlank() || currentOffer.description.isBlank()) {
            onFailure("Please fill in all fields.")
            return
        }

        if (currentUser == null) {
            onFailure("User not authenticated.")
            return
        }

        viewModelScope.launch {
            db.collection("users")
                .document(currentUser.uid)
                .collection("skillOffers")
                .add(currentOffer)
                .addOnSuccessListener {
                    onSuccess()
                    skillOffer.value = SkillOffer() // Reset form
                }
                .addOnFailureListener { e ->
                    onFailure("Failed to submit skill offer: ${e.message}")
                }
        }
    }
}