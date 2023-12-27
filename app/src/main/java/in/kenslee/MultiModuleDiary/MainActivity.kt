package `in`.kenslee.MultiModuleDiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import `in`.kenslee.MultiModuleDiary.data.database.ImagesToDeleteDao
import `in`.kenslee.MultiModuleDiary.data.database.ImagesToUploadDao
import `in`.kenslee.MultiModuleDiary.navigation.Screen
import `in`.kenslee.MultiModuleDiary.navigation.SetupNavGraph
import `in`.kenslee.MultiModuleDiary.ui.theme.MultiModuleDiaryTheme
import `in`.kenslee.MultiModuleDiary.utils.Constants.APP_ID
import `in`.kenslee.MultiModuleDiary.utils.retryDeletingImageFromFirebase
import `in`.kenslee.MultiModuleDiary.utils.retryUploadingImageToFirebase
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imagesToUploadDao: ImagesToUploadDao
    @Inject
    lateinit var imagesToDeleteDao: ImagesToDeleteDao
    override fun onCreate(savedInstanceState: Bundle?) {
        var keepSplash = true
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition{
            keepSplash
        }
        FirebaseApp.initializeApp(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MultiModuleDiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SetupNavGraph(
                        startDestination = getStartDestination(),
                        navController = navController,
                        onDataLoaded = {keepSplash = false}
                    )
                }
            }
        }
        cleanUpCheck(
            scope = lifecycleScope,
            imagesToUploadDao = imagesToUploadDao,
            imagesToDeleteDao = imagesToDeleteDao
        )
    }
}


private fun cleanUpCheck(
    scope: CoroutineScope,
    imagesToUploadDao: ImagesToUploadDao,
    imagesToDeleteDao: ImagesToDeleteDao
){
    scope.launch(Dispatchers.IO) {
        val result = imagesToUploadDao.getAllImages()
        result.forEach {
            retryUploadingImageToFirebase(
                imageToUpload = it,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imagesToUploadDao.cleanupImage(it.id)
                    }
                }
            )
        }
        val imagesToDelete = imagesToDeleteDao.getAllImages()
        imagesToDelete.forEach {
            retryDeletingImageFromFirebase(it){
                scope.launch(Dispatchers.IO) {
                    imagesToDeleteDao.cleanUpImages(it.id)
                }
            }
        }
    }
}

private fun getStartDestination() : String{
    val user = App.create(APP_ID).currentUser
    return if(user != null && user.loggedIn)
        Screen.Home.route
    else
        Screen.Authentication.route
}
