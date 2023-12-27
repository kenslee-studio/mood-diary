package `in`.kenslee.MultiModuleDiary.presentation.screen.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    onSuccessfulFirebaseAuthentication:(String) -> Unit,
    onFailureFirebaseAuthentication:(String)->Unit,
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
            val credentials = GoogleAuthProvider.getCredential(tokenId,null)
            FirebaseAuth.getInstance().signInWithCredential(credentials)
                .addOnSuccessListener {
                    onSuccessfulFirebaseAuthentication(tokenId)
                }.addOnFailureListener{
                    onFailureFirebaseAuthentication(it.message ?: "Exception authenticating user")
                }
        },
        onDialogDismissed = {message ->
            onDialogDismissed(message)
        }
    )
}