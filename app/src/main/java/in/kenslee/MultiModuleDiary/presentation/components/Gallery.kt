package `in`.kenslee.MultiModuleDiary.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import `in`.kenslee.MultiModuleDiary.model.GalleryImage
import `in`.kenslee.MultiModuleDiary.model.GalleryState
import `in`.kenslee.MultiModuleDiary.utils.Elevation
import kotlin.math.max


// values are passed from outside so that they
// can later be changed as per wish.
@Composable
fun Gallery(
    modifier : Modifier = Modifier,
    images: List<Uri>,
    imageSize: Dp = 40.dp,
    spaceBetween: Dp = 10.dp,
    imageShape: CornerBasedShape = Shapes().small,
) {
    BoxWithConstraints(modifier = modifier) {
        val numberOfVisibleImages = remember{
            derivedStateOf{
               max(
                   a = 0 ,
                   b = maxWidth.div(spaceBetween + imageSize).toInt().minus(1)
               )
            }
        }
        val remainingImages = remember{
            derivedStateOf{
                images.size - numberOfVisibleImages.value
            }
        }

        Row {
            images.take(numberOfVisibleImages.value).forEach {image ->
                AsyncImage(
                    modifier = Modifier
                        .clip(imageShape)
                        .size(imageSize),
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(image)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Saved images",
                )
                Spacer(modifier = Modifier.width(spaceBetween))
            }
            if(remainingImages.value > 0){
                LastImageOverlay(
                    imageSize = imageSize,
                    imageShape = imageShape,
                    remainingImages = remainingImages.value
                )
            }
        }
    }
}
@Composable
fun GalleryUploader(
    modifier: Modifier = Modifier,
    imageSize: Dp = 60.dp,
    imageShape: CornerBasedShape = MaterialTheme.shapes.medium,
    spaceBetween: Dp = 12.dp,
    galleryState: GalleryState,
    onAddClicked: () -> Unit,
    onImageClicked: (GalleryImage) -> Unit,
    onImageSelect: (Uri) -> Unit
){
    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8),
        onResult = {images ->
            images.forEach { uri ->
                onImageSelect(uri)
            }
        }
    )

    BoxWithConstraints(modifier = modifier) {
        val numberOfVisibleImages = remember{
            derivedStateOf{
                max(
                    a = 0 ,
                    b = maxWidth.div(spaceBetween + imageSize).toInt().minus(2)
                )
            }
        }
        val remainingImages = remember{
            derivedStateOf{
                galleryState.images.size - numberOfVisibleImages.value
            }
        }

        Row {
            AddImageButton(
                imageSize = imageSize,
                imageShape = imageShape,
                onClick = {
                    onAddClicked()
                    multiplePhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )

            Spacer(modifier = Modifier.width(spaceBetween))

            galleryState.images.take(numberOfVisibleImages.value).forEach {galleryImage ->
                AsyncImage(
                    modifier = Modifier
                        .clip(imageShape)
                        .size(imageSize)
                        .clickable{onImageClicked(galleryImage)},
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(galleryImage.image)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Saved images",
                )
                Spacer(modifier = Modifier.width(spaceBetween))
            }
            if(remainingImages.value > 0){
                LastImageOverlay(
                    imageSize = imageSize,
                    imageShape = imageShape,
                    remainingImages = remainingImages.value
                )
            }
        }
    }


}

@Composable
fun LastImageOverlay(
    imageSize: Dp,
    imageShape: CornerBasedShape,
    remainingImages : Int
){
    Box (
        contentAlignment = Alignment.Center
    ){
        Surface (
            modifier = Modifier
                .clip(imageShape)
                .size(imageSize),
            color = MaterialTheme.colorScheme.primaryContainer,
            content = {}
        )
        Text(
            text = "+$remainingImages",
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
@Composable
fun AddImageButton(
    imageSize: Dp,
    imageShape: CornerBasedShape,
    onClick: () -> Unit
){
    Box (
        contentAlignment = Alignment.Center
    ){
        Surface (
            modifier = Modifier
                .clip(imageShape)
                .size(imageSize),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = Elevation.level2,
            onClick = onClick,
            content = {}
        )
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "add icon"
        )
    }
}