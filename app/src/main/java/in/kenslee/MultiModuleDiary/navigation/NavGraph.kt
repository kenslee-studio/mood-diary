package `in`.kenslee.MultiModuleDiary.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import `in`.kenslee.auth.navigation.authenticationRoute
import `in`.kenslee.home.nav.homeRoute
import `in`.kenslee.utils.Screen
import `in`.kenslee.write.nav.writeRoute

@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authenticationRoute(
            navigateToHome = {
            navController.popBackStack()
            navController.navigate(Screen.Home.route) },
            onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
           navigateToAuth = {
               navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
           },
            onDataLoaded = onDataLoaded,
            navigateToWriteWithArgs = {diaryId ->
                navController.navigate(Screen.Write.passDiaryId(diaryId = diaryId))
            }
        )
        writeRoute(
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }
}
