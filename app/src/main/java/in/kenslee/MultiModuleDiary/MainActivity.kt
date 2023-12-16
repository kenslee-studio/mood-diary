package `in`.kenslee.MultiModuleDiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import `in`.kenslee.MultiModuleDiary.navigation.Screen
import `in`.kenslee.MultiModuleDiary.navigation.SetupNavGraph
import `in`.kenslee.MultiModuleDiary.ui.theme.MultiModuleDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            MultiModuleDiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SetupNavGraph(startDestination = Screen.Authentication.route, navController = navController )
                }
            }
        }
    }
}