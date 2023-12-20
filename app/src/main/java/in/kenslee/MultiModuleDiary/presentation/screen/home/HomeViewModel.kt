package `in`.kenslee.MultiModuleDiary.presentation.screen.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.kenslee.MultiModuleDiary.data.repository.Diaries
import `in`.kenslee.MultiModuleDiary.data.repository.MongoDb
import `in`.kenslee.MultiModuleDiary.utils.RequestState
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)
        private set

    init {
        observeAllDiaries()
    }

    private fun observeAllDiaries(){
        viewModelScope.launch {
            MongoDb.getAllDiaries().collect{
                diaries.value = it
            }
        }
    }
}