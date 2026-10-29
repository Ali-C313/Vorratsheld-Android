package com.example.vorratsheld.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.vorratsheld.data.enums.LegalType
import com.example.vorratsheld.service.AppLock
import com.example.vorratsheld.ui.screens.applock.AppLockSettingsScreen
import com.example.vorratsheld.ui.screens.auth.LoginScreen
import com.example.vorratsheld.ui.screens.auth.RegisterScreen
import com.example.vorratsheld.ui.screens.editprofile.EditProfileScreen
import com.example.vorratsheld.ui.screens.help.HelpScreen
import com.example.vorratsheld.ui.screens.language.LanguageScreen
import com.example.vorratsheld.ui.screens.legal.LegalScreen
import com.example.vorratsheld.ui.screens.lock.LockScreen
import com.example.vorratsheld.ui.screens.onboarding.OnboardingScreen
import com.example.vorratsheld.ui.screens.overview.OverviewScreen
import com.example.vorratsheld.ui.screens.productdetail.ProductDetailScreen
import com.example.vorratsheld.ui.screens.productinfo.ProductInfoScreen
import com.example.vorratsheld.ui.screens.scan.ScanScreen
import com.example.vorratsheld.ui.screens.settings.SettingsScreen
import com.example.vorratsheld.ui.screens.shoppinglist.ShoppingListScreen
import com.example.vorratsheld.ui.screens.splash.SplashScreen
import com.example.vorratsheld.ui.viewmodel.AuthViewModel
import com.example.vorratsheld.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

// Splash nur einmal pro App-Start zeigen (nicht nochmal nach einem Sprachwechsel)
private var introShownThisSession = false

// Navigation der App plus Sperrbildschirm darüber
@Composable
fun VorratsheldNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()

    // Nach Splash/Onboarding: eingeloggt -> Übersicht, sonst Login
    val afterIntro: () -> Screen = {
        if (authViewModel.isLoggedIn.value) Screen.Overview else Screen.Login
    }
    val startDestination = if (introShownThisSession) afterIntro() else Screen.Splash

    val locked by AppLock.locked.collectAsState()
    var introDone by remember { mutableStateOf(introShownThisSession) }

    // App im Hintergrund oder wieder da, AppLock entscheidet ob gesperrt wird
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { AppLock.onBackground() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { AppLock.onForeground() }

    // Abmelden, Konto löschen oder "PIN vergessen": zurück zum Login, Backstack leeren
    LaunchedEffect(isLoggedIn) {
        val destination = navController.currentBackStackEntry?.destination ?: return@LaunchedEffect
        val onAuthScreen = destination.hasRoute<Screen.Login>() ||
                destination.hasRoute<Screen.Register>() ||
                destination.hasRoute<Screen.Splash>() ||
                destination.hasRoute<Screen.Onboarding>()
        if (!isLoggedIn && !onAuthScreen) {
            navController.navigate(Screen.Login) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    // Nach Login/Registrierung direkt in die Übersicht
    val toOverview: () -> Unit = {
        navController.navigate(Screen.Overview) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable<Screen.Splash> {
                SplashScreen(
                    onFinished = {
                        introShownThisSession = true
                        introDone = true
                        val target = if (settingsViewModel.isOnboardingDone()) afterIntro() else Screen.Onboarding
                        navController.navigate(target) {
                            popUpTo(Screen.Splash) { inclusive = true }
                        }
                    }
                )
            }
            composable<Screen.Onboarding> {
                OnboardingScreen(
                    onFinished = {
                        settingsViewModel.finishOnboarding()
                        navController.navigate(afterIntro()) {
                            popUpTo(Screen.Onboarding) { inclusive = true }
                        }
                    }
                )
            }
            composable<Screen.Login> {
                LoginScreen(
                    onLoggedIn = toOverview,
                    onNavigateToRegister = { navController.navigate(Screen.Register) }
                )
            }
            composable<Screen.Register> {
                RegisterScreen(
                    onRegistered = toOverview,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Screen.Overview> {
                OverviewScreen(
                    onNavigateToScan = { navController.navigate(Screen.Scan) },
                    onNavigateToShoppingList = { navController.navigate(Screen.ShoppingList) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings) },
                    onNavigateToInfo = { productId -> navController.navigate(Screen.ProductInfo(productId = productId)) },
                    onNavigateToEdit = { productId -> navController.navigate(Screen.ProductDetail(productId = productId)) }
                )
            }
            composable<Screen.Scan> {
                ScanScreen(
                    onBarcodeScanned = { barcode ->
                        navController.navigate(Screen.ProductDetail(barcode = barcode)) {
                            // Zurück zum Scan-Screen soll nicht möglich sein, Übersicht bleibt im Backstack
                            popUpTo(Screen.Scan) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Screen.Settings> {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToProfile = { navController.navigate(Screen.EditProfile) },
                    onNavigateToLanguage = { navController.navigate(Screen.Language) },
                    onNavigateToAppLock = { navController.navigate(Screen.SecuritySettings) },
                    onNavigateToHelp = { navController.navigate(Screen.Help) },
                    onNavigateToLegal = { type -> navController.navigate(Screen.Legal(type.name)) }
                )
            }
            composable<Screen.EditProfile> {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }
            composable<Screen.Language> {
                LanguageScreen(onBack = { navController.popBackStack() })
            }
            composable<Screen.Help> {
                HelpScreen(onBack = { navController.popBackStack() })
            }
            composable<Screen.SecuritySettings> {
                AppLockSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable<Screen.Legal> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.Legal>()
                LegalScreen(
                    onBack = { navController.popBackStack() },
                    type = LegalType.valueOf(route.type)
                )
            }
            // productId bzw. barcode liest das ViewModel selbst aus der Route (SavedStateHandle)
            composable<Screen.ProductDetail> {
                ProductDetailScreen(
                    onBack = { navController.popBackStack() },
                    // Info-Screen darunter zeigt das gelöschte Produkt nicht mehr, deshalb bis zur Übersicht zurück
                    onDeleted = { navController.popBackStack(Screen.Overview, inclusive = false) }
                )
            }
            composable<Screen.ProductInfo> {
                ProductInfoScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { productId -> navController.navigate(Screen.ProductDetail(productId = productId)) }
                )
            }
            composable<Screen.ShoppingList> {
                ShoppingListScreen(onBack = { navController.popBackStack() })
            }
        }

        // Sperre erst nach dem Splash und nur, wenn jemand eingeloggt ist
        if (locked && introDone && isLoggedIn) {
            LockScreen(
                onUnlocked = { AppLock.unlock() },
                onForgotPin = {
                    // Abmelden reicht, den Rest erledigt der LaunchedEffect oben
                    authViewModel.logout()
                    AppLock.unlock()
                }
            )
        }
    }
}
