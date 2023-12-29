package `in`.kenslee.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.kenslee.utils.RequestState
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun HomeScreen(
    dateIsSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit,
    diaries : `in`.kenslee.mongo.repository.Diaries,
    onMenuClicked: () -> Unit,
    navigateToWrite: () -> Unit,
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    navigateToWriteWithArgs: (String) ->Unit
    ) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked,
        onDeleteAllClicked = onDeleteAllClicked
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopBar(
                    scrollBehavior = scrollBehavior,
                    onMenuClicked = onMenuClicked,
                    dateIsSelected = dateIsSelected,
                    onDateSelected = onDateSelected,
                    onDateReset = onDateReset
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = navigateToWrite) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "new diary icon")
                }
            },
            content = {
                when(diaries){
                    is RequestState.Error -> {
                        EmptyPage(
                            title = "Error",
                            subTitle = "${diaries.error.message}"
                        )
                    }
                    RequestState.Idle -> {

                    }
                    RequestState.Loading -> {
                        Box (
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            CircularProgressIndicator()
                        }
                    }
                    is RequestState.Success -> {
                        HomeContent(
                            paddingValues = it,
                            diaryNotes = diaries.data,
                            onClick = navigateToWriteWithArgs
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked : () -> Unit,
    onDeleteAllClicked: () -> Unit,
    content : @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(100.dp),
                        painter = painterResource(`in`.kenslee.ui.R.drawable.logo) ,
                        contentDescription = "brand logo",
                    )
                }
                NavigationDrawerItem(
                    label= {
                       Row (
                           modifier = Modifier.padding(12.dp)
                       ){
                           Image(
                               painter = painterResource(`in`.kenslee.ui.R.drawable.google_logo) ,
                               contentDescription = "google logo",
                           )
                           Spacer(
                               modifier = Modifier.width(12.dp)
                           )
                           Text(
                              text = "SignOut"
                           )
                       }
                    },
                    selected = false,
                    onClick = onSignOutClicked
                ) 
                NavigationDrawerItem(
                    label= {
                       Row (
                           modifier = Modifier.padding(12.dp)
                       ){
                           Icon(
                               imageVector = Icons.Default.Delete ,
                               contentDescription = "delete icon",
                           )
                           Spacer(
                               modifier = Modifier.width(12.dp)
                           )
                           Text(
                              text = "Delete all diaries"
                           )
                       }
                    },
                    selected = false,
                    onClick = onDeleteAllClicked
                )
            }
        },
        drawerState = drawerState,
        content = content
    )
}

