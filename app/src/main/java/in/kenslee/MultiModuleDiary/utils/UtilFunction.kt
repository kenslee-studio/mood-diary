package `in`.kenslee.MultiModuleDiary.utils

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import `in`.kenslee.MultiModuleDiary.data.database.ImageToDelete
import `in`.kenslee.MultiModuleDiary.data.database.ImageToUpload
import io.realm.kotlin.types.RealmInstant
import java.time.Instant

fun fetchImagesFromFirebase(
    remoteImagePaths: List<String>,
    onImageDownload: (Uri) -> Unit,
    onImageDownloadFailed: (Exception) -> Unit,
    onReadyToDisplay: () -> Unit
){
    if(remoteImagePaths.isNotEmpty()){
        remoteImagePaths.forEachIndexed { index, remoteImagePath ->
            if(remoteImagePath.trim().isNotEmpty()){
                FirebaseStorage.getInstance().reference.child(remoteImagePath.trim()).downloadUrl
                    .addOnSuccessListener {
                        Log.d("myApp", "uri = $it")
                        onImageDownload(it)
                        if(remoteImagePath.lastIndex == index){
                            onReadyToDisplay()
                        }
                    }.addOnFailureListener {
                        Log.d("remote", "remote path = $remoteImagePath exception = $it")
                        onImageDownloadFailed(it)
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


fun RealmInstant.toInstant() : Instant {
    val sec = this.epochSeconds
    val nano = this.nanosecondsOfSecond

    return if(sec >= 0){
        Instant.ofEpochSecond(sec , nano.toLong())
    }else{
        Instant.ofEpochSecond(sec -1 , 1_000_000 + nano.toLong())
    }
}

fun Instant.toRealmInstant(): RealmInstant{
    val sec: Long = this.epochSecond
    val nano: Int = this.nano
    return if(sec >= 0)
                RealmInstant.from(sec, nano)
            else
                RealmInstant.from(sec + 1 , -1_000_000 + nano)
}

inline fun <reified T : Enum<T>> Int.toEnum(): T? {
    return enumValues<T>().first{ it.ordinal == this }
}