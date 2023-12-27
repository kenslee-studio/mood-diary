package `in`.kenslee.MultiModuleDiary.navigation

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import `in`.kenslee.MultiModuleDiary.model.GalleryImage
import `in`.kenslee.MultiModuleDiary.model.Mood
import `in`.kenslee.MultiModuleDiary.presentation.components.DisplayAlertDialog
import `in`.kenslee.MultiModuleDiary.presentation.screen.auth.AuthenticationScreen
import `in`.kenslee.MultiModuleDiary.presentation.screen.auth.AuthenticationViewModel
import `in`.kenslee.MultiModuleDiary.presentation.screen.home.HomeScreen
import `in`.kenslee.MultiModuleDiary.presentation.screen.home.HomeViewModel
import `in`.kenslee.MultiModuleDiary.presentation.screen.write.WriteScreen
import `in`.kenslee.MultiModuleDiary.presentation.screen.write.WriteViewModel
import `in`.kenslee.MultiModuleDiary.utils.Constants.APP_ID
import `in`.kenslee.MultiModuleDiary.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import `in`.kenslee.MultiModuleDiary.utils.RequestState
import `in`.kenslee.MultiModuleDiary.utils.toEnum
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

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

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
){
    composable(route = Screen.Authentication.route){
        val oneTapSignInState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        LaunchedEffect(
            key1 = Unit,
            block = { onDataLoaded() }
        )

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
            onSuccessfulFirebaseAuthentication = { tokenId ->
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
            },
            onFailureFirebaseAuthentication = {message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            }
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit
    ){
    composable(route = Screen.Home.route){

        val context = LocalContext.current
        val viewModel : HomeViewModel = hiltViewModel()
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
        HomeScreen(
            diaries = diaries,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            dateIsSelected = viewModel.dateIsSelected,
            onDateSelected = {viewModel.getDiaries(it)},
            onDateReset = {viewModel.getDiaries(null)},
            navigateToWrite = navigateToWrite,
            drawerState = drawerState,
            onSignOutClicked = {signOutDialogOpened = true},
            navigateToWriteWithArgs = navigateToWriteWithArgs,
            onDeleteAllClicked = {deleteAllDialogOpened = true}
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

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit){
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY){
           type = NavType.StringType
           nullable = true
           defaultValue = null }
        )
    ){

        val pagerState = rememberPagerState(pageCount = { Mood.values().size })
        val pageNumber by remember { derivedStateOf { pagerState.currentPage } }
        val viewModel: WriteViewModel = hiltViewModel()
        val galleryState = viewModel.galleryState
        val screenState = viewModel.screenState
        val context = LocalContext.current

        WriteScreen(
            onBackPressed = onBackPressed,
            onDeleteConfirm = {
               viewModel.deleteDiary(
                   onSuccess = {
                       Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                       onBackPressed()
                   },
                   onError = {
                       Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                   }
               )
            },
            pagerState = pagerState,
            screenState = screenState,
            onDescriptionChanged = {viewModel.setDescription(it)},
            onTitleChanged = {viewModel.setTitle(it)},
            moodName = {Mood.values()[pageNumber].name},
            onSaveClicked = {
                viewModel.upsertDiary(
                    diary = it.apply{mood = pageNumber.toEnum<Mood>().toString() },
                    onSuccess = onBackPressed,
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDateTimeUpdated = {zonedDateTime ->
                viewModel.setDateTime(zonedDateTime)
            },
            galleryState = galleryState,
            onAddClicked = {

            },
            onImageDeleteClicked = {imageToDelete ->
                galleryState.removeImage(imageToDelete)
            },
            onImageSelect = {uri ->
                val type = context.contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"
                val remotePath = viewModel.getRemotePath(uri , type)
                galleryState.addImages(GalleryImage(uri , remotePath))
            }
        )
    }
}