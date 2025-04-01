package com.pccoer.skillmesh

import android.content.Context
import android.os.Bundle
import android.widget.Toast
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pccoer.skillmesh.ui.screens.CategorySelectionScreen
import com.pccoer.skillmesh.ui.screens.CreateProfileScreen
import com.pccoer.skillmesh.ui.theme.SkillmeshTheme
import com.pccoer.skillmesh.viewmodel.ProfileViewModel
import com.pccoer.skillmesh.viewmodel.checkUserProfileAndNavigate
import com.pccoer.skillmesh.SkillOfferScreen
import com.pccoer.skillmesh.viewmodel.SkillOfferViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

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
                                    launchSingleTop = true
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
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("verifyEmail") {
                VerifyEmailScreen(
                    onVerificationSuccess = {
                        navController.navigate("profile") {
                            popUpTo("verifyEmail") { inclusive = true }
                            popUpTo("signUp") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel()
                CreateProfileScreen(
                    viewModel = profileViewModel,
                    onProfileSaved = {
                        navController.navigate("categorySelection") {
                            popUpTo("profile") { inclusive = true }
                            popUpTo("verifyEmail") { inclusive = true }
                            popUpTo("signUp") { inclusive = true }
                            popUpTo("signIn") { inclusive = true }
                            popUpTo("welcome") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("categorySelection") {
                CategorySelectionScreen(onCategorySelected = { categories ->
                    saveCategories(categories, navController) // Save the categories
                })
            }
            composable("skillOffer") {
                val skillOfferViewModel: SkillOfferViewModel = viewModel()
                SkillOfferScreen(viewModel = skillOfferViewModel, onSkillOfferSubmitted = {
                    navController.navigate("main")
                })
            }

            composable("main") {
//                HomeScreen()
            }
        }
    }
}

fun saveCategories(categories: List<String>, navController: NavController){
    val userId = Firebase.auth.currentUser?.uid
    if(userId != null){
        Firebase.firestore.collection("users").document(userId).update("categories", categories)
            .addOnSuccessListener {
                navController.navigate("skillOffer")
            }
            .addOnFailureListener{
                // handle failure.
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
@Preview(showBackground = true)
@Composable
fun CategorySelectionScreenPreview() {
    SkillmeshTheme {
        CategorySelectionScreen(onCategorySelected = {})
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