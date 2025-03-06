package com.pccoer.skillmesh

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun VerifyEmailScreen(onVerificationSuccess: () -> Unit) {
    var verificationStatus by remember { mutableStateOf<VerificationStatus>(VerificationStatus.Initial) }
    val auth: FirebaseAuth = Firebase.auth
    val user = auth.currentUser
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verify Email",
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(64.dp))
        when (verificationStatus) {
            VerificationStatus.Initial -> {
                Text(text = "Please verify your email.")
            }

            VerificationStatus.Loading -> {
                CircularProgressIndicator(color = Color.Black)
                Text(text = "Checking verification status...")
            }

            VerificationStatus.Verified -> {
                Text(text = "Email Verified!")
            }

            VerificationStatus.NotVerified -> {
                Text(text = "Email not verified.")
            }

            VerificationStatus.Error -> {
                Text(text = "Error checking verification.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                verificationStatus = VerificationStatus.Loading
                user?.reload()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (user.isEmailVerified) {
                            verificationStatus = VerificationStatus.Verified
                            onVerificationSuccess()
                        } else {
                            verificationStatus = VerificationStatus.NotVerified
                        }
                    } else {
                        verificationStatus = VerificationStatus.Error
                        Toast.makeText(
                            context,
                            "Error, please check your network connection.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Refresh Verification Status")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                user?.sendEmailVerification()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Verification email sent.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error sending verification email.", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Resend Verification Email")
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (user?.isEmailVerified == true) {
            verificationStatus = VerificationStatus.Verified
            onVerificationSuccess()
        }
    }
}

enum class VerificationStatus {
    Initial, Loading, Verified, NotVerified, Error
}