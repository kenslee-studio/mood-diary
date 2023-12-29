package `in`.kenslee.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.kenslee.mongo.repository.MongoDb.deleteAllDiaries
import `in`.kenslee.mongo.repository.MongoDb.getAllDiaries
import `in`.kenslee.mongo.repository.MongoDb.getFilteredDiaries
import `in`.kenslee.utils.RequestState
import `in`.kenslee.utils.connectivity.ConnectivityObserver.Status.Available
import `in`.kenslee.utils.connectivity.ConnectivityObserver.Status.Unavailable
import `in`.kenslee.utils.connectivity.NetworkConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    networkConnectivityObserver: NetworkConnectivityObserver,
    private val imagesToDeleteDao: `in`.kenslee.mongo.database.ImagesToDeleteDao
) : ViewModel() {

    private var filterDiariesJob : Job? = null
    private var allDiariesJob : Job? = null

    private var network by mutableStateOf(Unavailable)
    var diaries: MutableState<`in`.kenslee.mongo.repository.Diaries> = mutableStateOf(RequestState.Idle)
        private set
    var dateIsSelected by mutableStateOf(false)
        private set

    init {
        getDiaries()
        viewModelScope.launch {
            networkConnectivityObserver.observe().collect { network = it }
        }
    }

    fun getDiaries(zonedDateTime: ZonedDateTime? = null){
        dateIsSelected = zonedDateTime != null
        diaries.value = RequestState.Loading
        filterDiariesJob?.cancel()
        allDiariesJob?.cancel()
        if(dateIsSelected){
            observeFilteredDiaries(zonedDateTime!!)
        }else{
            observeAllDiaries()
        }
    }

    private fun observeAllDiaries() {
        allDiariesJob = viewModelScope.launch {
            getAllDiaries().collect {
                diaries.value = it
            }
        }
    }
    private fun observeFilteredDiaries(zonedDateTime: ZonedDateTime) {
        filterDiariesJob = viewModelScope.launch {
            getFilteredDiaries(zonedDateTime).collect {
                diaries.value = it
            }
        }
    }


    fun deleteAllData(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        when (network) {
            Available -> {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val imagesDirectory = "images/${userId}"
                val storage = FirebaseStorage.getInstance().reference
                storage.child(imagesDirectory)
                    .listAll()
                    .addOnSuccessListener { listResult ->
                        listResult.items.forEach { storageRef ->
                            val path = "images/${userId}/${storageRef.name}"
                            storage
                                .child(path)
                                .delete()
                                .addOnFailureListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    imagesToDeleteDao.addImageToDelete(imageToDelete = `in`.kenslee.mongo.database.ImageToDelete(remoteImagePath = path))
                                }
                            }
                        }
                        viewModelScope.launch {
                            val result = deleteAllDiaries()
                            if(result is RequestState.Success){
                                onSuccess()
                            }else if (result is RequestState.Error){
                                onError(result.error)
                            }
                        }
                    }
            }

            else -> {
                onError(Exception("No or unstable internet connection"))
            }
        }
    }
}