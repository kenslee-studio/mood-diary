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
import androidx.navigation.compose.rememberNavController
import `in`.kenslee.MultiModuleDiary.navigation.Screen
import `in`.kenslee.MultiModuleDiary.navigation.SetupNavGraph
import `in`.kenslee.MultiModuleDiary.ui.theme.MultiModuleDiaryTheme
import `in`.kenslee.MultiModuleDiary.utils.Constants.APP_ID
import io.realm.kotlin.mongodb.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var keepSplash = true
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition{
            keepSplash
        }
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
    }

}

private fun getStartDestination() : String{
    val user = App.create(APP_ID).currentUser
    return if(user != null && user.loggedIn)
        Screen.Home.route
    else
        Screen.Authentication.route
}
