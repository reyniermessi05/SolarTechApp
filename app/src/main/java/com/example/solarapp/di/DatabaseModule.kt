package com.example.solarapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.solarapp.data.AppDatabase
import com.example.solarapp.data.FaultCode
import com.example.solarapp.data.FaultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        provider: Provider<FaultDao>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "solar_app_db"
        ).fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val faultDao = provider.get()
                    val faultsList = mutableListOf<FaultCode>()
                    try {
                        context.assets.open("faults_hem2.csv").bufferedReader().useLines { lines ->
                            // Skip header
                            val iterator = lines.iterator()
                            if (iterator.hasNext()) iterator.next()

                            while (iterator.hasNext()) {
                                val line = iterator.next()
                                // Simple CSV parsing (handling quotes)
                                val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                                    .map { it.removePrefix("\"").removeSuffix("\"").replace("\\n", "\n") }

                                if (tokens.size >= 6) {
                                    faultsList.add(
                                        FaultCode(
                                            equipmentType = tokens[0],
                                            faultCode = tokens[1],
                                            issueTitle = tokens[2],
                                            troubleshootingSteps = tokens[3],
                                            issueTitleEs = tokens[4],
                                            troubleshootingStepsEs = tokens[5]
                                        )
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (faultsList.isNotEmpty()) {
                        faultDao.insertAll(faultsList)
                    }
                }
            }
        })
        .build()
    }

    @Provides
    @Singleton
    fun provideFaultDao(appDatabase: AppDatabase): FaultDao {
        return appDatabase.faultDao()
    }
}
