package `in`.kenslee.MultiModuleDiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImagesToUploadDao {
    @Query("SELECT * FROM ImageToUpload ORDER BY id ASC")
    suspend fun getAllImages(): List<ImageToUpload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImageToUpload(imageToUpload: ImageToUpload)

    @Query("DELETE FROM ImageToUpload WHERE id = :imageId")
    suspend fun cleanupImage(imageId: Int)
}