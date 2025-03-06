package com.pccoer.skillmesh

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pccoer.skillmesh.viewmodel.AuthResult
import com.pccoer.skillmesh.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.ktx.firestore

@Composable
fun SignInScreen(
    navController: NavController, // Pass NavController here
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val authResult by viewModel.authResult.collectAsState()

    val context = LocalContext.current

    // Handle authentication results
    LaunchedEffect(authResult) {
        when (authResult) {
            is AuthResult.Success -> {
                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                checkUserProfileAndNavigate(context, navController, onSignInSuccess) // ✅ Pass NavController here
            }
            is AuthResult.Error -> {
                Toast.makeText(context, (authResult as AuthResult.Error).message, Toast.LENGTH_SHORT).show()
            }
            null -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center, // Center vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        Text(
            text = "Log In",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.signIn(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Increased height
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Log In", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // Increased height
                    .border(1.dp, Color.Black, RoundedCornerShape(24.dp)), // Rounded border
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Sign Up", color = Color.Black)
            }
        }
    }
}

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
                    // ✅ If profile exists, go to HomeScreen
                    onSignInSuccess()
                } else {
                    // ❗ If profile is missing, navigate to CreateProfileScreen
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