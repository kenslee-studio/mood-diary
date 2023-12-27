package `in`.kenslee.MultiModuleDiary.data.repository

import `in`.kenslee.MultiModuleDiary.model.Diary
import `in`.kenslee.MultiModuleDiary.utils.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import java.time.ZonedDateTime


typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>
interface MongoRepository {
    fun configureTheRealm()
    fun getAllDiaries(): Flow<Diaries>
    fun getFilteredDiaries(zonedDateTime: ZonedDateTime): Flow<Diaries>
    suspend fun getSelectedDiary(diaryId : ObjectId): RequestState<Diary>
    suspend fun addNewDiary(diary : Diary): RequestState<Diary>
    suspend fun updateDiary(diary: Diary): RequestState<Diary>
    suspend fun deleteDiary(id : ObjectId): RequestState<Diary>
    suspend fun deleteAllDiaries():RequestState<Boolean>

}