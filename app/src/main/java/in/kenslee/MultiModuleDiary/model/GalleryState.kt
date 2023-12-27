package `in`.kenslee.MultiModuleDiary.model

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember


class GalleryState{
    val images = mutableStateListOf<GalleryImage>()
    val imagesToBeDeleted = mutableStateListOf<GalleryImage>()

    fun addImages(galleryImage: GalleryImage){
        images.add(galleryImage)
    }

    fun removeImage(galleryImage: GalleryImage){
        images.remove(galleryImage)
        imagesToBeDeleted.add(galleryImage)
    }

    fun clearImagesToBeDeleted(){
        imagesToBeDeleted.clear()
    }
}
data class GalleryImage (
    val image: Uri,
    val remoteImagePath: String = ""
)