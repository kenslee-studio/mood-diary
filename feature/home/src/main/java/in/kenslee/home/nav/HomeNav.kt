package `in`.kenslee.home.nav

import android.widget.Toast
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import `in`.kenslee.ui.components.DisplayAlertDialog
import `in`.kenslee.utils.Constants
import `in`.kenslee.utils.RequestState
import `in`.kenslee.utils.Screen
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit
){
    composable(route = Screen.Home.route){

        val context = LocalContext.current
        val viewModel : `in`.kenslee.home.HomeViewModel = hiltViewModel()
        val diaries by viewModel.diaries
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var signOutDialogOpened by remember{ mutableStateOf(false) }
        var deleteAllDialogOpened by remember{ mutableStateOf(false) }

        LaunchedEffect(
            key1 = diaries,
            block = {
                if(diaries !is RequestState.Loading){
                    onDataLoaded()
                }
            }
        )
        `in`.kenslee.home.HomeScreen(
            diaries = diaries,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            dateIsSelected = viewModel.dateIsSelected,
            onDateSelected = { viewModel.getDiaries(it) },
            onDateReset = { viewModel.getDiaries(null) },
            navigateToWrite = navigateToWrite,
            drawerState = drawerState,
            onSignOutClicked = { signOutDialogOpened = true },
            navigateToWriteWithArgs = navigateToWriteWithArgs,
            onDeleteAllClicked = { deleteAllDialogOpened = true }
        )

        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out of this google account",
            dialogOpened = signOutDialogOpened,
            closeDialog = {signOutDialogOpened = false},
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    App.create(Constants.APP_ID).currentUser?.logOut()
                    withContext(Dispatchers.Main.immediate){
                        navigateToAuth()
                    }
                }
            }
        )
        DisplayAlertDialog(
            title = "Delete All",
            message = "Are you sure you want to delete all the diary created?",
            dialogOpened = deleteAllDialogOpened,
            closeDialog = {deleteAllDialogOpened = false},
            onYesClicked = {
                viewModel.deleteAllData(
                    onSuccess = {
                        Toast.makeText(context, "All diaries deleted successfully", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onError = {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                )
                deleteAllDialogOpened = false
            }
        )
    }
}
