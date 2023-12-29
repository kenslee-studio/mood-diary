package `in`.kenslee.auth.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import `in`.kenslee.utils.Screen


fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
){
    composable(route = Screen.Authentication.route){
        val oneTapSignInState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()
        val viewModel: `in`.kenslee.auth.AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        LaunchedEffect(
            key1 = Unit,
            block = { onDataLoaded() }
        )

        `in`.kenslee.auth.AuthenticationScreen(
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
                    onSuccess = { isLoggedIn ->
                        if (isLoggedIn) {
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
            onFailureFirebaseAuthentication = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            }
        )
    }
}
