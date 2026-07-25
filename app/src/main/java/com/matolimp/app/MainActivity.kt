package com.matolimp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matolimp.app.ui.HomeScreen
import com.matolimp.app.ui.OnboardingScreen
import com.matolimp.app.ui.PaywallScreen
import com.matolimp.app.ui.ProblemScreen
import com.matolimp.app.ui.ProfileScreen
import com.matolimp.app.ui.ThemeScreen
import com.matolimp.app.ui.theme.MatOlimpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MatOlimpTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val repo = App.repo(LocalContext.current)
    val profile by repo.profile.collectAsState(initial = null)

    val p = profile
    if (p == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Стартовый экран фиксируем один раз, чтобы NavHost не пересоздавался.
    val start = remember { if (p.onboarded) "home" else "onboarding" }
    AppNav(start)
}

@Composable
private fun AppNav(startDestination: String) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(onDone = {
                nav.navigate("home") { popUpTo("onboarding") { inclusive = true } }
            })
        }
        composable("home") {
            HomeScreen(
                onOpenTheme = { id -> nav.navigate("theme/$id") },
                onOpenProblem = { pid -> nav.navigate("problem/$pid") },
                onOpenProfile = { nav.navigate("profile") }
            )
        }
        composable(
            route = "theme/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            ThemeScreen(
                themeId = id,
                onOpenProblem = { pid -> nav.navigate("problem/$pid") },
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            route = "paywall/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            PaywallScreen(
                themeId = id,
                onPurchased = {
                    nav.navigate("theme/$id") { popUpTo("paywall/$id") { inclusive = true } }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(onBack = { nav.popBackStack() })
        }
        composable(
            route = "problem/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            ProblemScreen(problemId = id, onBack = { nav.popBackStack() })
        }
    }
}
