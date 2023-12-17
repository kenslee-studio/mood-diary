package `in`.kenslee.MultiModuleDiary.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import `in`.kenslee.MultiModuleDiary.presentation.components.DisplayAlertDialog
import `in`.kenslee.MultiModuleDiary.presentation.screen.auth.AuthenticationScreen
import `in`.kenslee.MultiModuleDiary.presentation.screen.auth.AuthenticationViewModel
import `in`.kenslee.MultiModuleDiary.presentation.screen.home.HomeScreen
import `in`.kenslee.MultiModuleDiary.utils.Constants.APP_ID
import `in`.kenslee.MultiModuleDiary.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

@Composable
fun SetupNavGraph(startDestination: String, navController: NavHostController) {
    NavHost(navController = navController,
        startDestination = startDestination) {
        authenticationRoute(navigateToHome = {
            navController.popBackStack()
            navController.navigate(Screen.Home.route)
        })
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
           navigateToAuth = {
               navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
           }
        )
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

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToAuth: () -> Unit
    ){
    composable(route = Screen.Home.route){

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var signOutDialogOpened by remember{ mutableStateOf(false) }

        HomeScreen(
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            onFilterClicked = {},
            navigateToWrite = navigateToWrite,
            drawerState = drawerState,
            onSignOutClicked = {signOutDialogOpened = true}
        )

        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out of this google account",
            dialogOpened = signOutDialogOpened,
            closeDialog = {signOutDialogOpened = false},
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    App.create(APP_ID).currentUser?.logOut()
                    withContext(Dispatchers.Main.immediate){
                        navigateToAuth()
                    }
                }
            }
        )
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