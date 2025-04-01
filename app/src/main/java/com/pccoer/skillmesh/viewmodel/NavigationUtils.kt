package com.pccoer.skillmesh.viewmodel

// NavigationUtils.kt



import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
fun checkUserProfileAndNavigate(
    context: Context,
    navController: NavController,
    onSignInSuccess: () -> Unit
) {
    val userId = Firebase.auth.currentUser?.uid
    if (userId != null) {
        Firebase.firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getBoolean("profileCompleted") == true) {
                    if (document.contains("categories")) {
                        // User has profile and categories, navigate to skillOffer
                        navController.navigate("skillOffer")
                    } else {
                        // User has profile, but no categories, navigate to category selection
                        navController.navigate("categorySelection")
                    }
                } else {
                    // User has not completed profile
                    Toast.makeText(context, "Complete your profile", Toast.LENGTH_SHORT).show()
                    navController.navigate("profile") {
                        popUpTo("signIn") { inclusive = true }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to check profile", Toast.LENGTH_SHORT).show()
            }
    }
}