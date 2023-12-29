package `in`.kenslee.mongo.repository

import android.util.Log
import `in`.kenslee.utils.model.Diary
import `in`.kenslee.utils.Constants.APP_ID
import `in`.kenslee.utils.RequestState
import `in`.kenslee.utils.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object MongoDb : MongoRepository {
    private val app = App.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        user?.let {
            val config = SyncConfiguration.Builder(
                user,
                setOf(Diary::class)
            )
                .initialSubscriptions(rerunOnOpen = true) { sub ->
                    add(
                        query = sub.query(
                            clazz = Diary::class,
                            query = "ownerId == $0",
                            user.id
                        ),
                        name = "User's Diary"
                    )
                }
                .build()
            realm = Realm.open(config)
        }
    }

    override fun getAllDiaries(): Flow<Diaries> {
        return user?.let {
            try {
                Log.d(
                    "myApp",
                    "userid = ${user.id}"
                )
                realm.query(
                    clazz = Diary::class,
                    query = "ownerId == $0",
                    user.id
                )
                    .sort(
                        property = "date",
                        sortOrder = Sort.DESCENDING
                    )
                    .asFlow()
                    .map { result ->
                        Log.d(
                            "myApp",
                            "getAllDiaries() result = ${result.list}"
                        )
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }

            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } ?: flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
    }

    override suspend fun getSelectedDiary(diaryId: ObjectId) = withContext(Dispatchers.IO) {
        user?.let {
            try {
                val diary = realm.query(
                    Diary::class,
                    "_id == $0",
                    diaryId
                ).find().first()
                RequestState.Success(diary)
            } catch (e: Exception) {
                RequestState.Error(e)
            }
        } ?: RequestState.Error(UserNotAuthenticatedException())
    }

    override suspend fun addNewDiary(diary: Diary) = withContext(Dispatchers.IO) {
        user?.let {
            realm.write {
                try {
                    val addDiary = copyToRealm(
                        diary.apply {
                            this.ownerId = user.id
                        }
                    )
                    RequestState.Success(data = addDiary)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } ?: RequestState.Error(UserNotAuthenticatedException())
    }

    override suspend fun updateDiary(diary: Diary) = withContext(Dispatchers.IO) {
        user?.let {
            realm.write {
                val queriedDiary = query(
                    Diary::class,
                    query = "_id == $0",
                    diary._id
                ).first().find()
                queriedDiary?.let {
                    it.mood = diary.mood
                    it.date = diary.date
                    it.title = diary.title
                    it.description = diary.description
                    it.images = diary.images

                    RequestState.Success(data = queriedDiary)

                } ?: RequestState.Error(Exception("Diary doesn't exist"))
            }
        } ?: RequestState.Error(UserNotAuthenticatedException())
    }

    override suspend fun deleteDiary(id: ObjectId) = withContext(Dispatchers.IO) {
        user?.let { user ->
            realm.write {
                val queriedDiary = query(
                    Diary::class,
                    query = "_id == $0 && ownerId == $1",
                    id,
                    user.id
                ).first().find()
                queriedDiary?.let {

                    delete(queriedDiary)
                    RequestState.Success(data = queriedDiary)

                } ?: RequestState.Error(Exception("Diary doesn't exist"))
            }
        } ?: RequestState.Error(UserNotAuthenticatedException())
    }

    override suspend fun deleteAllDiaries() = withContext(Dispatchers.IO) {
        user?.let { user ->
            realm.write {
                val queriedDiary = query(
                    Diary::class,
                    query = "ownerId == $0",
                    user.id
                ).find()
                delete(queriedDiary)
                RequestState.Success(data = true)
            }
        }
    } ?: RequestState.Error(UserNotAuthenticatedException())

    override fun getFilteredDiaries(zonedDateTime: ZonedDateTime) : Flow<Diaries> {
        return user?.let {
            try {
                val dateBeforeSetDate = RealmInstant.from(LocalDateTime.of(zonedDateTime.toLocalDate().plusDays(1) , LocalTime.MIDNIGHT).toEpochSecond(zonedDateTime.offset), 0)
                val dateAfterSetDate = RealmInstant.from(LocalDateTime.of(zonedDateTime.toLocalDate() , LocalTime.MIDNIGHT).toEpochSecond(zonedDateTime.offset) , 0)
                realm.query(clazz = Diary::class, query = "ownerId == $0 && date < $1 AND date > $2", user.id , dateAfterSetDate , dateBeforeSetDate )
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map { result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.date
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }

            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } ?: flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
    }

}

private class UserNotAuthenticatedException : Exception("User is not logged in.")