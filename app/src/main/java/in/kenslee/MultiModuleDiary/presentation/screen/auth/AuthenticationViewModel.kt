package `in`.kenslee.MultiModuleDiary.presentation.screen.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.kenslee.MultiModuleDiary.utils.Constants.APP_ID
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthenticationViewModel : ViewModel() {
    var loadingState = mutableStateOf(false)
        private set
    fun setLoading(loading : Boolean){
        loadingState.value = loading
    }

    fun signInWithMongoAtlas(
        tokenId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ){
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO){
                    App.create(APP_ID)
                        .login(Credentials.google(tokenId , GoogleAuthType.ID_TOKEN))
                        .loggedIn
                }
                withContext(Dispatchers.Main){
                    onSuccess(result)
                }
            }catch (e : Exception){
                withContext(Dispatchers.Main){
                    onError(e)
                }
            }
        }
    }
}