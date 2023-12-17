package `in`.kenslee.MultiModuleDiary.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import `in`.kenslee.MultiModuleDiary.presentation.screen.auth.AuthenticationScreen
import `in`.kenslee.MultiModuleDiary.presentation.screen.auth.AuthenticationViewModel
import `in`.kenslee.MultiModuleDiary.utils.Constants.APP_ID
import `in`.kenslee.MultiModuleDiary.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

@Composable
fun SetupNavGraph(startDestination: String, navController: NavHostController) {
    NavHost(navController = navController,
        startDestination = startDestination) {
        authenticationRoute(navigateToHome = {
            navController.popBackStack()
            navController.navigate(Screen.Home.route)
        })
        homeRoute()
        writeRoute()
    }
}

fun NavGraphBuilder.authenticationRoute(navigateToHome : () -> Unit){
    composable(route = Screen.Authentication.route){
        val oneTapSignInState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState

        AuthenticationScreen(
            loadingState = loadingState,
            oneTapState = oneTapSignInState,
            messageBarState = messageBarState,
            onClick = {
                oneTapSignInState.open()
                viewModel.setLoading(true)
            },
            onDialogDismissed = {
                messageBarState.addError(Exception(it))
                viewModel.setLoading(false)
            },
            onTokenReceived = {tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId,
                    onSuccess = {isLoggedIn ->
                        if(isLoggedIn){
                            messageBarState.addSuccess("Successfully Authenticated")
                            navigateToHome()
                        }
                        viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(it)
                        viewModel.setLoading(false)
                    }
                )
            }
        )
    }
}

fun NavGraphBuilder.homeRoute(){
    composable(route = Screen.Home.route){
        val scope = rememberCoroutineScope()
        Column {
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    App.create(APP_ID).currentUser?.logOut()
                }
            }) {
                Text(text = "Logout")
            }
        }
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