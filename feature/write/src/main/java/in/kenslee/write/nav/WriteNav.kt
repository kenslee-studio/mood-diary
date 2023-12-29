package `in`.kenslee.write.nav

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import `in`.kenslee.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import `in`.kenslee.utils.Screen
import `in`.kenslee.utils.model.Mood
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.kenslee.ui.GalleryImage
import `in`.kenslee.utils.toEnum
import `in`.kenslee.write.*

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
            moodName = { Mood.values()[pageNumber].name},
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