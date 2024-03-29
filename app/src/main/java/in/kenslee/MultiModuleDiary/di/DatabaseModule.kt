package `in`.kenslee.MultiModuleDiary.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.kenslee.MultiModuleDiary.connectivity.NetworkConnectivityObserver
import `in`.kenslee.MultiModuleDiary.data.database.ImagesDatabase
import `in`.kenslee.MultiModuleDiary.utils.Constants.IMAGES_DATABASE
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context : Context
    ): ImagesDatabase{
        return Room.databaseBuilder(
            context = context,
            klass = ImagesDatabase::class.java,
            name = IMAGES_DATABASE
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideFirstDao(database: ImagesDatabase) = database.imageToUploadDao()
    @Singleton
    @Provides
    fun provideSecondDao(database: ImagesDatabase) = database.imageToDeleteDao()

    @Singleton
    @Provides
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context : Context
    ) = NetworkConnectivityObserver(context = context)
}