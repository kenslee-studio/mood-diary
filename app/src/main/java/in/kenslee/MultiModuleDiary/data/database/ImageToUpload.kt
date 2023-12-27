package `in`.kenslee.MultiModuleDiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ImageToUpload(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remotePath: String,
    val imageUri: String,
    val sessionUri: String
)