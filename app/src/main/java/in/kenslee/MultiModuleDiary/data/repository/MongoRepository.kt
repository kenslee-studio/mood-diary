package `in`.kenslee.MultiModuleDiary.data.repository

import `in`.kenslee.MultiModuleDiary.model.Diary
import `in`.kenslee.MultiModuleDiary.utils.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>
interface MongoRepository {
    fun configureTheRealm()
    fun getAllDiaries(): Flow<Diaries>
}