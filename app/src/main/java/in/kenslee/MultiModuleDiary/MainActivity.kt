package `in`.kenslee.MultiModuleDiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import dagger.hilt.android.AndroidEntryPoint
import `in`.kenslee.utils.Screen
import `in`.kenslee.MultiModuleDiary.navigation.SetupNavGraph
import `in`.kenslee.mongo.database.ImageToDelete
import `in`.kenslee.mongo.database.ImageToUpload
import `in`.kenslee.ui.theme.MultiModuleDiaryTheme
import `in`.kenslee.utils.Constants.APP_ID
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imagesToUploadDao: `in`.kenslee.mongo.database.ImagesToUploadDao
    @Inject
    lateinit var imagesToDeleteDao: `in`.kenslee.mongo.database.ImagesToDeleteDao
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
    imagesToUploadDao: `in`.kenslee.mongo.database.ImagesToUploadDao,
    imagesToDeleteDao: `in`.kenslee.mongo.database.ImagesToDeleteDao
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
            retryDeletingImageFromFirebase(it) {
                scope.launch(Dispatchers.IO) {
                    imagesToDeleteDao.cleanUpImages(it.id)
                }
            }
        }
    }
}


fun retryUploadingImageToFirebase(
    imageToUpload: ImageToUpload,
    onSuccess: () -> Unit
){
    val storage = FirebaseStorage.getInstance().reference
    storage.child(imageToUpload.remotePath).putFile(
        imageToUpload.imageUri.toUri(),
        storageMetadata {  },
        imageToUpload.sessionUri.toUri()
    ).addOnSuccessListener {
        onSuccess()
    }
}

fun retryDeletingImageFromFirebase(
    imageToDelete: ImageToDelete,
    onSuccess: () -> Unit
){
    val storage = FirebaseStorage.getInstance().reference
    storage.child(imageToDelete.remoteImagePath).delete()
        .addOnSuccessListener {
            onSuccess()
        }
}


private fun getStartDestination() : String{
    val user = App.create(APP_ID).currentUser
    return if(user != null && user.loggedIn)
        Screen.Home.route
    else
        Screen.Authentication.route
}
