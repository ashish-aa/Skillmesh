package com.pccoer.skillmesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pccoer.skillmesh.ui.screens.CreateProfileScreen
import com.pccoer.skillmesh.ui.theme.SkillmeshTheme
import com.pccoer.skillmesh.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        auth = Firebase.auth

        setContent {
            SkillmeshTheme {
                MainScreen(auth)
            }
        }
    }
}

@Composable
fun MainScreen(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val startDestination = if (auth.currentUser != null) "main" else "welcome"
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("welcome") {
                WelcomeScreen(
                    onSignInClicked = { navController.navigate("signIn") },
                    onSignUpClicked = { navController.navigate("signUp") }
                )
            }
            composable("signIn") {
                val context = LocalContext.current // ✅ Get context here

                SignInScreen(
                    navController = navController, // ✅ Pass NavController
                    onSignInSuccess = {
                        checkUserProfileAndNavigate(
                            context = context, // ✅ Pass Context here
                            navController = navController, // ✅ Pass NavController here
                            onSignInSuccess = {
                                navController.navigate("main") {
                                    popUpTo("welcome") { inclusive = true }
                                    popUpTo("signIn") { inclusive = true }
                                }
                            }
                        )
                    },
                    onNavigateToSignUp = { navController.navigate("signUp") }
                )
            }

            composable("signUp") {
                SignUpScreen(
                    onNavigateToSignIn = { navController.navigate("signIn") },
                    onNavigateToVerifyEmail = {
                        navController.navigate("verifyEmail") {
                            popUpTo("signUp") { inclusive = true }
                        }
                    }
                )
            }
            composable("verifyEmail") {
                VerifyEmailScreen(
                    onVerificationSuccess = { navController.navigate("profile") {
                        popUpTo("verifyEmail") {inclusive = true}
                    } }
                )
            }
            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel()
                CreateProfileScreen(
                    viewModel = profileViewModel,
                    onProfileSaved = {
                        navController.navigate("main") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                )
            }

            composable("main") {
//                HomeScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

class PreviewLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SkillmeshTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    val context = LocalContext.current
    val lifecycleOwner = PreviewLifecycleOwner()
    CompositionLocalProvider(
        LocalContext provides context,
        LocalLifecycleOwner provides lifecycleOwner
    ) {
        SkillmeshTheme {
            WelcomeScreen(onSignInClicked = {}, onSignUpClicked = {})
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SignInScreenPreview() {
//    val context = LocalContext.current
//    val lifecycleOwner = PreviewLifecycleOwner()
//    CompositionLocalProvider(
//        LocalContext provides context,
//        LocalLifecycleOwner provides lifecycleOwner
//    ) {
//        SkillmeshTheme {
//            SignInScreen(onSignInSuccess = {}, onNavigateToSignUp = {})
//        }
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun SignUpScreenPreview() {
//    val context = LocalContext.current
//    val lifecycleOwner = PreviewLifecycleOwner()
//    // Initialize Firebase for the preview
//    FirebaseApp.initializeApp(context)
//    CompositionLocalProvider(
//        LocalContext provides context,
//        LocalLifecycleOwner provides lifecycleOwner
//    ) {
//        SkillmeshTheme {
//            SignUpScreen(onSignUpSuccess = {}, onNavigateToSignIn = {})
//        }
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun VerifyEmailScreenPreview() {
//    val context = LocalContext.current
//    val lifecycleOwner = PreviewLifecycleOwner()
//    CompositionLocalProvider(
//        LocalContext provides context,
//        LocalLifecycleOwner provides lifecycleOwner
//    ) {
//        SkillmeshTheme {
//            VerifyEmailScreen(onRefreshClicked = {})
//        }
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun CreateProfileScreenPreview() {
//    val context = LocalContext.current
//    val lifecycleOwner = PreviewLifecycleOwner()
//    CompositionLocalProvider(
//        LocalContext provides context,
//        LocalLifecycleOwner provides lifecycleOwner
//    ) {
//        SkillmeshTheme {
//            CreateProfileScreen(viewModel: ProfileViewModel,onSaveClicked = {})
//        }
//    }
//}