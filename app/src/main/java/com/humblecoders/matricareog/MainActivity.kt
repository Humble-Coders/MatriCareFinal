package com.humblecoders.matricareog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.humblecoders.matricareog.repository.*
import com.humblecoders.matricareog.screens.*
import com.humblecoders.matricareog.screens.authScreens.*
import com.humblecoders.matricareog.screens.inputScreens.*
import com.humblecoders.matricareog.screens.maternalGuide.*
import com.humblecoders.matricareog.screens.welcomeScreens.*
import com.humblecoders.matricareog.ui.theme.MatricareogTheme
import com.humblecoders.matricareog.viewmodels.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var userRepository: UserRepository
    private lateinit var matriCareRepository: MatriCareRepository
    private lateinit var medicalHistoryRepository: MedicalHistoryRepository
    private lateinit var reportRepository: ReportRepository

    private lateinit var authViewModel: AuthViewModel
    private lateinit var matriCareViewModel: MatriCareViewModel
    private lateinit var medicalHistoryViewModel: MedicalHistoryViewModel
    private lateinit var reportAnalysisViewModel: ReportAnalysisViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        userRepository = UserRepository(auth, firestore, DataStoreManager(applicationContext))
        matriCareRepository = MatriCareRepository(auth, firestore)
        medicalHistoryRepository = MedicalHistoryRepository(firestore)
        reportRepository = ReportRepository()

        authViewModel = AuthViewModel(userRepository, auth)
        matriCareViewModel = MatriCareViewModel(matriCareRepository)
        medicalHistoryViewModel = MedicalHistoryViewModel(medicalHistoryRepository)
        reportAnalysisViewModel = ReportAnalysisViewModel(reportRepository)


        setContent {
            MatricareogTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val currentUser by authViewModel.currentUser.collectAsState()

                    NavHost(navController = navController, startDestination = Routes.SPLASH) {

                        composable(Routes.SPLASH) {
                            SplashScreen(
                                onNavigateToHome = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                },
                                onNavigateToWelcome = {
                                    navController.navigate(Routes.WELCOME_1) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                },
                                onNavigateToTerms = {
                                    navController.navigate(Routes.TERMS_ACCEPTANCE) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Routes.TERMS_ACCEPTANCE) {
                            TermsAcceptanceScreen(
                                onAcceptTerms = {
                                    // Mark terms as accepted and navigate to welcome
                                    navController.navigate(Routes.WELCOME_1) {
                                        popUpTo(Routes.TERMS_ACCEPTANCE) { inclusive = true }
                                    }
                                },
                                onViewTerms = {
                                    navController.navigate(Routes.TERMS_AND_CONDITIONS)
                                },
                                onViewPrivacyPolicy = {
                                    navController.navigate(Routes.PRIVACY_POLICY)
                                }
                            )
                        }

                        composable(Routes.TERMS_AND_CONDITIONS) {
                            TermsAndConditionsScreen()
                        }

                        composable(Routes.PRIVACY_POLICY) {
                            PrivacyPolicyScreen(
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                onBackPressed = {
                                    navController.popBackStack()
                                },
                                onViewTerms = {
                                    navController.navigate(Routes.TERMS_AND_CONDITIONS)
                                },
                                onViewPrivacyPolicy = {
                                    navController.navigate(Routes.PRIVACY_POLICY)
                                },
                                onLogout = {
                                    // Call logout on the authViewModel
                                    authViewModel.logout()
                                    // Navigate to auth choice
                                    navController.navigate(Routes.AUTH_CHOICE) {
                                        popUpTo(Routes.HOME) { inclusive = true }
                                    }
                                },
                                onDeleteAccount = {
                                    // Call delete account on the authViewModel
                                    authViewModel.deleteAccount()
                                    // Navigate to auth choice
                                    navController.navigate(Routes.AUTH_CHOICE) {
                                        popUpTo(Routes.HOME) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Routes.WELCOME_1) {
                            WelcomeScreenOne(
                                onNextClicked = { navController.navigate(Routes.WELCOME_2) },
                                onSkipClicked = { navController.navigate(Routes.GET_STARTED) },
                                currentPageIndex = 0
                            )
                        }

                        composable(Routes.WELCOME_2) {
                            WelcomeScreenTwo(
                                onNextClicked = { navController.navigate(Routes.WELCOME_3) },
                                onSkipClicked = { navController.navigate(Routes.GET_STARTED) },
                                currentPageIndex = 1
                            )
                        }

                        composable(Routes.WELCOME_3) {
                            WelcomeScreenThree(
                                onNextClicked = { navController.navigate(Routes.GET_STARTED) },
                                onSkipClicked = { navController.navigate(Routes.GET_STARTED) },
                                currentPageIndex = 2
                            )
                        }

                        composable(Routes.GET_STARTED) {
                            GetStarted(
                                onGetStartedClick = { navController.navigate(Routes.AUTH_CHOICE) },
                                currentPageIndex = 3
                            )
                        }

                        composable(Routes.AUTH_CHOICE) {
                            LoginSignup(
                                onSignUpClick = { navController.navigate(Routes.SIGNUP) },
                                onLogInClick = { navController.navigate(Routes.LOGIN) }
                            )
                        }

                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onNavigateToSignUp = { navController.navigate(Routes.SIGNUP) },
                                onNavigateToHome = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.WELCOME_1) { inclusive = true }
                                    }
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Routes.SIGNUP) {
                            SignUpScreen(
                                onNavigateToLogin = { navController.popBackStack() },
                                onNavigateToHome = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.WELCOME_1) { inclusive = true }
                                    }
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Routes.HOME) {
                            val userId = currentUser?.uid ?: ""
                            HomeScreen(
                                authViewModel = authViewModel,
                                onTrackHealthClicked = {
                                    navController.navigate(Routes.medicalHistory1Route(userId))
                                },
                                onMaternalGuideClicked = {
                                    navController.navigate(Routes.MATERNAL_GUIDE)
                                },
                                onReportHistoryClicked = {
                                    navController.navigate(Routes.MATRICARE)
                                },
                                onSettingsClicked = {
                                    navController.navigate(Routes.SETTINGS)
                                },
                                onDietClicked = {
                                    navController.navigate(Routes.DIET_PLAN)
                                },
                                onYogaClicked = {
                                    navController.navigate(Routes.YOGA_EXERCISES)
                                },
                                onDoClicked = {
                                    navController.navigate(Routes.DOS_AND_DONTS)
                                },
                                onChatbotClicked = {
                                    navController.navigate(Routes.CHATBOT)
                                },
                                onProfileClicked = {
                                    navController.navigate(Routes.PROFILE)
                                }
                            )
                        }

                        composable(Routes.PROFILE) {
                            ProfileScreen(
                                authViewModel = authViewModel,
                                onBackClick = { navController.popBackStack() },
                                onEditProfileClick = { navController.navigate(Routes.EDIT_PROFILE) },
                                onPrivacySecurityClick = { navController.navigate(Routes.PRIVACY_SECURITY) },
                                onLanguageAccessibilityClick = { navController.navigate(Routes.LANGUAGE_ACCESSIBILITY) }
                            )
                        }

                        composable(Routes.EDIT_PROFILE) {
                            EditProfileScreen(
                                authViewModel = authViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.PRIVACY_SECURITY) {
                            PrivacySecurityScreen(
                                onBackClick = { navController.popBackStack() },
                                onViewPrivacyPolicy = { navController.navigate(Routes.PRIVACY_POLICY) }
                            )
                        }

                        composable(Routes.LANGUAGE_ACCESSIBILITY) {
                            LanguageAccessibilityScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Routes.MEDICAL_HISTORY_1,
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) {
                            val userId = it.arguments?.getString("userId") ?: ""
                            InputScreenOne(
                                userId = userId,
                                navigateBack = { navController.popBackStack() },
                                navigateToScreenTwo = {
                                    navController.navigate(Routes.medicalHistory2Route(userId))
                                },
                                viewModel = medicalHistoryViewModel
                            )
                        }

                        composable(
                            route = Routes.MEDICAL_HISTORY_2,
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) {
                            val userId = it.arguments?.getString("userId") ?: ""
                            InputScreenTwo(
                                userId = userId,
                                onBackPressed = { navController.popBackStack() },
                                onContinuePressed = {
                                    navController.navigate(Routes.reportAnalysisRoute(userId)) {
                                        popUpTo(Routes.MEDICAL_HISTORY_1) { inclusive = true }
                                    }
                                },
                                viewModel = medicalHistoryViewModel
                            )
                        }

                        composable(
                            route = Routes.REPORT_ANALYSIS,
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) {
                            val userId = it.arguments?.getString("userId") ?: ""
                            ReportAnalysisScreen(
                                userId = userId,
                                onBackClick = { navController.popBackStack() },
                                onShareClick = { shareReport() },
                                reportViewModel = reportAnalysisViewModel,
                                medicalHistoryViewModel = medicalHistoryViewModel,
                                authViewModel = authViewModel
                            )
                        }

                        composable(Routes.MATRICARE) {
                            GraphReportScreen(
                                onBackClick = { navController.popBackStack() },
                                viewModel = matriCareViewModel
                            )
                        }

                        composable(Routes.MATERNAL_GUIDE) {
                            MaternalGuideScreen(
                                onBackClick = { navController.popBackStack() },
                                onCardClick = { cardId ->
                                    when (cardId) {
                                        "diet_plan" -> navController.navigate(Routes.DIET_PLAN)
                                        "yoga_exercises" -> navController.navigate(Routes.YOGA_EXERCISES)
                                        "dos_donts" -> navController.navigate(Routes.DOS_AND_DONTS)
                                    }
                                }
                            )
                        }

                        composable(Routes.DIET_PLAN) {
                            DietPlanScreen(
                                onBackClick = { navController.popBackStack() },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Routes.DOS_AND_DONTS) {
                            DosAndDontsScreen(
                                onBackClick = { navController.popBackStack() },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Routes.YOGA_EXERCISES) {
                            YogaExercisesScreen(
                                onBackClick = { navController.popBackStack() },
                                authViewModel = authViewModel
                            )
                        }

                    }
                }
            }
        }
    }

    private fun shareReport() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out my health report from MatriCare!")
            putExtra(Intent.EXTRA_SUBJECT, "Health Report - MatriCare")
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Share Report"))
        } catch (e: Exception) {
            Log.e("MainActivity", "Error sharing report", e)
        }
    }
}

object Routes {
    const val SPLASH = "SplashScreen"
    const val TERMS_ACCEPTANCE = "TermsAcceptance"
    const val TERMS_AND_CONDITIONS = "TermsAndConditions"
    const val PRIVACY_POLICY = "PrivacyPolicy"
    const val SETTINGS = "Settings"
    const val WELCOME_1 = "WelcomeScreenOne"
    const val WELCOME_2 = "WelcomeScreenTwo"
    const val WELCOME_3 = "WelcomeScreenThree"
    const val GET_STARTED = "GetStarted"
    const val AUTH_CHOICE = "auth_choice"
    const val LOGIN = "LoginScreen"
    const val SIGNUP = "SignUpScreen"
    const val HOME = "HomeScreen"
    const val MATERNAL_GUIDE = "MaternalGuideScreen"
    const val DIET_PLAN = "DietPlanScreen"
    const val YOGA_EXERCISES = "YogaExercisesScreen"
    const val DOS_AND_DONTS = "DosAndDontsScreen"
    const val MEDICAL_HISTORY_1 = "WelcomeScreenOne/{userId}"
    const val MEDICAL_HISTORY_2 = "WelcomeScreenTwo/{userId}"
    const val REPORT_ANALYSIS = "ReportAnalysisScreen/{userId}"
    const val MATRICARE = "GraphReportScreen"
    const val CHATBOT = "ChatbotScreen"
    const val PROFILE = "ProfileScreen"
    const val EDIT_PROFILE = "EditProfileScreen"
    const val PRIVACY_SECURITY = "PrivacySecurityScreen"
    const val LANGUAGE_ACCESSIBILITY = "LanguageAccessibilityScreen"

    fun medicalHistory1Route(userId: String) = "WelcomeScreenOne/$userId"
    fun medicalHistory2Route(userId: String) = "WelcomeScreenTwo/$userId"
    fun reportAnalysisRoute(userId: String) = "ReportAnalysisScreen/$userId"
}
