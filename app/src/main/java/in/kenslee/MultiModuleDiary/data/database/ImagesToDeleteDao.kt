package `in`.kenslee.MultiModuleDiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImagesToDeleteDao{
    @Query("SELECT * FROM ImageToDelete ORDER BY id ASC")
    suspend fun getAllImages(): List<ImageToDelete>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImageToDelete(imageToDelete: ImageToDelete)

    @Query("DELETE FROM ImageToDelete WHERE id = :imageId")
    suspend fun cleanUpImages(imageId: Int)
}