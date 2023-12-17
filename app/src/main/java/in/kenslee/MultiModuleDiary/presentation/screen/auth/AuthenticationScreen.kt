package `in`.kenslee.MultiModuleDiary.presentation.screen.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import `in`.kenslee.MultiModuleDiary.utils.Constants.CLIENT_ID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthenticationScreen(
    oneTapState: OneTapSignInState,
    messageBarState: MessageBarState,
    loadingState: Boolean,
    onClick: () -> Unit,
    onTokenReceived:(String) -> Unit,
    onDialogDismissed:(String) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ){
        ContentWithMessageBar(messageBarState = messageBarState) {
            AuthenticationContent(loadingState = loadingState, onClick = onClick)
        }
    }
    OneTapSignInWithGoogle(
        state = oneTapState,
        clientId = CLIENT_ID,
        onTokenIdReceived = { tokenId ->
            onTokenReceived(tokenId)
        },
        onDialogDismissed = {message ->
            onDialogDismissed(message)
        }
    )
}