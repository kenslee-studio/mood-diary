package `in`.kenslee.MultiModuleDiary.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import `in`.kenslee.MultiModuleDiary.navigation.Screen
import `in`.kenslee.MultiModuleDiary.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY

@Composable
fun SetupNavGraph(startDestination: String, navController: NavHostController) {
    NavHost(navController = navController,
        startDestination = startDestination) {
        authenticationRoute()
        homeRoute()
        writeRoute()
    }
}

fun NavGraphBuilder.authenticationRoute(){
    composable(route = Screen.Authentication.route){

    }
}

fun NavGraphBuilder.homeRoute(){
    composable(route = Screen.Home.route){

    }
}

fun NavGraphBuilder.writeRoute(){
    composable(route = Screen.Write.route,
               arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY){
                   type = NavType.StringType
                   nullable = true
                   defaultValue = null
               })
        ){

    }
}