package `in`.kenslee.MultiModuleDiary.presentation.screen.write

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.kenslee.MultiModuleDiary.data.database.ImageToDelete
import `in`.kenslee.MultiModuleDiary.data.database.ImageToUpload
import `in`.kenslee.MultiModuleDiary.data.database.ImagesToDeleteDao
import `in`.kenslee.MultiModuleDiary.data.database.ImagesToUploadDao
import `in`.kenslee.MultiModuleDiary.data.repository.MongoDb
import `in`.kenslee.MultiModuleDiary.model.Diary
import `in`.kenslee.MultiModuleDiary.model.GalleryImage
import `in`.kenslee.MultiModuleDiary.model.GalleryState
import `in`.kenslee.MultiModuleDiary.model.Mood
import `in`.kenslee.MultiModuleDiary.utils.Constants
import `in`.kenslee.MultiModuleDiary.utils.RequestState
import `in`.kenslee.MultiModuleDiary.utils.fetchImagesFromFirebase
import `in`.kenslee.MultiModuleDiary.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime
import javax.inject.Inject


@SuppressLint("SuspiciousIndentation")
@HiltViewModel
class WriteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imagesToUploadDao: ImagesToUploadDao,
    private val imageToDeleteDao: ImagesToDeleteDao
) : ViewModel() {
    var galleryState by mutableStateOf(GalleryState())
    var screenState by mutableStateOf(ScreenState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument(){
        screenState = screenState.copy(selectedIdDiary = savedStateHandle[Constants.WRITE_SCREEN_ARGUMENT_KEY])
    }

    private fun fetchSelectedDiary(){
        screenState.selectedIdDiary?.let {
            viewModelScope.launch(Dispatchers.Main) {
                val diaryId = ObjectId.invoke(screenState.selectedIdDiary!!)
                val diary = MongoDb.getSelectedDiary(diaryId)
                if(diary is RequestState.Success){
                    screenState = screenState.copy(
                        selectedDiary = diary.data,
                        title = diary.data.title,
                        description = diary.data.description,
                        mood = Mood.valueOf(diary.data.mood)
                    )
                        fetchImagesFromFirebase(
                            remoteImagePaths = diary.data.images,
                            onReadyToDisplay = {},
                            onImageDownloadFailed = {},
                            onImageDownload = {downloadImages -> galleryState.addImages(
                                  GalleryImage(
                                      image = downloadImages,
                                      remoteImagePath = extractImagePath(remotePath = downloadImages.toString())
                                  )
                              )
                            },
                        )
                }
            }
        }
    }

    private fun extractImagePath(remotePath: String): String {
        val chunks = remotePath.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }

    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ){
        viewModelScope.launch {
            if(screenState.selectedIdDiary != null)
                updateDiary(diary , onSuccess , onError)
            else
                insertDiary(diary, onSuccess, onError)
        }
    }

    private suspend fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ){
        when(val result = MongoDb.addNewDiary(diary.apply { screenState.updatedDateTime?.let{date  = it}})){
            is RequestState.Error -> {
                onError(result.error.message!!)
            }
            RequestState.Idle -> Unit
            RequestState.Loading -> Unit
            is RequestState.Success -> {
                uploadImagesToFirebase()
                onSuccess()
            }
        }
    }

    private suspend fun updateDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ){
        when(val result = MongoDb.updateDiary(diary.apply {
            this._id = ObjectId.invoke(screenState.selectedIdDiary!!)
            this.date = screenState.updatedDateTime ?: screenState.selectedDiary!!.date
        })){
            is RequestState.Error -> {
                onError(result.error.message!!)
            }
            RequestState.Idle -> Unit
            RequestState.Loading -> Unit
            is RequestState.Success -> {
                uploadImagesToFirebase()
                deleteImagesFromFirebase(galleryState.imagesToBeDeleted.map { it.remoteImagePath })
                galleryState.clearImagesToBeDeleted()
                onSuccess()
            }
        }
    }

    fun deleteDiary(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ){
        viewModelScope.launch(Dispatchers.Main) {
            when(val result = MongoDb.deleteDiary(screenState.selectedDiary!!._id)){
                is RequestState.Error -> {
                    onError(result.error.message!!)
                }
                RequestState.Idle -> Unit
                RequestState.Loading -> Unit
                is RequestState.Success -> {
                    screenState.selectedDiary?.let{
                        deleteImagesFromFirebase(it.images)
                    }
                    onSuccess()
                }
            }
        }
    }

    fun setTitle(title: String) {
        screenState = screenState.copy(title = title)
    }

    fun setDescription(description: String) {
        screenState = screenState.copy(description = description)
    }

    fun setDateTime(zonedDateTime : ZonedDateTime){
        screenState = screenState.copy(updatedDateTime = zonedDateTime.toInstant()?.toRealmInstant())
    }

    private fun uploadImagesToFirebase(){
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { image ->
            val imagePath = storage.child(image.remoteImagePath)
            imagePath.putFile(image.image)
                .addOnProgressListener {taskSnapshot ->
                    val sessionUri = taskSnapshot.uploadSessionUri
                    if(sessionUri != null){
                        viewModelScope.launch(Dispatchers.IO) {
                            imagesToUploadDao.addImageToUpload(
                                ImageToUpload(
                                    remotePath = image.remoteImagePath,
                                    imageUri = image.image.toString(),
                                    sessionUri = sessionUri.toString()
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun deleteImagesFromFirebase(images : List<String>){
        val storage = FirebaseStorage.getInstance().reference
        images.forEach {remotePath ->
            storage
                .child(remotePath)
                .delete()
                .addOnFailureListener {
                    viewModelScope.launch(Dispatchers.IO) {
                        imageToDeleteDao.addImageToDelete(
                            ImageToDelete(
                                remoteImagePath = remotePath
                            )
                        )
                    }
                }

        }
    }

    fun getRemotePath(imageUri : Uri, imageType : String) =
        "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
            "${imageUri.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
}

data class ScreenState(
    val selectedIdDiary: String? = null,
    val selectedDiary: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null
)
