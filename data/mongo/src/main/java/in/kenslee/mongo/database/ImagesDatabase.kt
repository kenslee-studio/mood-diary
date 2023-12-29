package `in`.kenslee.mongo.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ImageToUpload::class , ImageToDelete::class],
    version = 2,
    exportSchema = true
)
abstract class ImagesDatabase : RoomDatabase() {
    abstract fun imageToUploadDao(): ImagesToUploadDao
    abstract fun imageToDeleteDao(): ImagesToDeleteDao
}