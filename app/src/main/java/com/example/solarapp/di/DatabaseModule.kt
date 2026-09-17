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
import org.apache.commons.csv.CSVFormat
import java.io.InputStreamReader

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
                        context.assets.open("faults_hem2.csv").use { inputStream ->
                            InputStreamReader(inputStream).use { reader ->
                                val records = CSVFormat.DEFAULT.builder()
                                    .setHeader()
                                    .setSkipHeaderRecord(true)
                                    .build()
                                    .parse(reader)
                                    
                                for (record in records) {
                                    // Make sure we unescape literal "\n" characters to actual newlines
                                    faultsList.add(
                                        FaultCode(
                                            equipmentType = record.get(0).trim(),
                                            faultCode = record.get(1).trim(),
                                            issueTitle = record.get(2).trim(),
                                            troubleshootingSteps = record.get(3).replace("\\n", "\n").trim(),
                                            issueTitleEs = record.get(4).trim(),
                                            troubleshootingStepsEs = record.get(5).replace("\\n", "\n").trim()
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
