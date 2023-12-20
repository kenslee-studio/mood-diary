package `in`.kenslee.MultiModuleDiary.data.repository

import android.util.Log
import `in`.kenslee.MultiModuleDiary.model.Diary
import `in`.kenslee.MultiModuleDiary.utils.Constants.APP_ID
import `in`.kenslee.MultiModuleDiary.utils.RequestState
import `in`.kenslee.MultiModuleDiary.utils.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId

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
                .log(LogLevel.ALL)
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
                realm.query(clazz = Diary::class, query = "ownerId == $0", user.id)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
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
}

private class UserNotAuthenticatedException : Exception("User is not logged in.")