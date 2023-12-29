package `in`.kenslee.utils

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.kenslee.ui.components.Gallery
import `in`.kenslee.ui.theme.Elevation
import `in`.kenslee.utils.model.Diary
import `in`.kenslee.utils.model.Mood
import io.realm.kotlin.ext.realmListOf
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

@Composable
fun DiaryHolder(
    diary: Diary,
    onClick: (String) -> Unit
) {
    val context = LocalContext.current
    val localDensity = LocalDensity.current
    var componentHeight by remember { mutableStateOf(0.dp) }
    var galleryOpened by remember { mutableStateOf(false) }
    var galleryLoading by remember { mutableStateOf(false) }
    val downloadImages = remember{mutableStateListOf<Uri>()}

    LaunchedEffect(
        key1 = galleryOpened,
        block = {
            if(galleryOpened && downloadImages.isEmpty()){
                galleryLoading = true
                fetchImagesFromFirebase(
                    remoteImagePaths = diary.images,
                    onImageDownload = {image ->
                       downloadImages.add(image)
                    },
                    onImageDownloadFailed = {
                        Toast.makeText(context, "Images not downloading, try again sometime later.", Toast.LENGTH_SHORT).show()
                        galleryLoading = false
                        galleryOpened = false
                    },
                    onReadyToDisplay = {
                        galleryLoading = false
                        galleryOpened = true
                    }
                )
            }
        }
    )

    Row(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember {
                    MutableInteractionSource()
                },
                onClick = { onClick(diary._id.toHexString())}
            )
    ) {
        Spacer(modifier = Modifier.width(14.dp))
        Surface(
            modifier = Modifier
                .width(2.dp)
                .height(componentHeight + 14.dp),
            tonalElevation = Elevation.level1,
            content = {}
        )
        Spacer(modifier = Modifier.width(20.dp))
        Surface(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .onGloballyPositioned {
                    componentHeight = with(localDensity) {
                        it.size.height.toDp()
                    }
                },
            tonalElevation = Elevation.level1
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DiaryHeader(
                    moodName = diary.mood,
                    time = diary.date.toInstant()
                )
                Text(
                    modifier = Modifier.padding(14.dp),
                    text = diary.description,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                if (diary.images.isNotEmpty()) {
                    ShowGalleryButton(
                        galleryOpened = galleryOpened,
                        galleryLoading = galleryLoading,
                        onClick = { galleryOpened = !galleryOpened }
                    )
                }
                AnimatedVisibility(
                    modifier = Modifier.padding(14.dp),
                    visible = galleryOpened,
                    enter =  fadeIn() + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Gallery(images = downloadImages)
                }
            }
        }
    }
}

@Composable
fun ShowGalleryButton(
    galleryOpened: Boolean,
    galleryLoading: Boolean,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            text =
            if (galleryOpened)
                if(galleryLoading)
                    "Loading"
                else
                    "Hide Gallery"
            else
                "Show Gallery"
            ,
            fontSize = MaterialTheme.typography.bodySmall.fontSize
        )
    }
}

@Composable
fun DiaryHeader(moodName: String, time: Instant) {
    val mood by remember {
        mutableStateOf(Mood.valueOf(moodName))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(mood.containerColor)
            .padding(
                horizontal = 14.dp,
                vertical = 7.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(mood.icon),
                contentDescription = "Mood icon"
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = mood.name,
                color = mood.contentColor,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
        }
        Text(
            text = SimpleDateFormat(
                "hh:mm a",
                Locale.US
            ).format(Date.from(time)),
            color = mood.contentColor,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiaryHolderPreview() {
    val dummyDiary = Diary().apply {
        title = "Today was the worst"
        description = "today i went to meet someone special but she didn't came and my heart broked completely hello"
        images = realmListOf("" , "" , "" , "" , "" , "" , "" , "" , "")
    }
    DiaryHolder(
        diary = dummyDiary,
        onClick = {}
    )
}