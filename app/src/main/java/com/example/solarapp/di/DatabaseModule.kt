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
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val faultDao = provider.get()
                    val mockData = listOf(
                        FaultCode(
                            equipmentType = "HEM Gen. 3",
                            faultCode = "ERR-001",
                            issueTitle = "Overheating",
                            troubleshootingSteps = "1. Medir voltaje en la entrada DC. 2. Si el voltaje es cero, revisar fusibles según el diagrama [Enlace: Schematic_HEM3_Rev4.pdf]"
                        ),
                        FaultCode(
                            equipmentType = "HEM Gen. 2",
                            faultCode = "ERR-002",
                            issueTitle = "Communication Loss",
                            troubleshootingSteps = "Check ethernet cables."
                        ),
                        FaultCode(
                            equipmentType = "DC/DC Converter Gen. 3",
                            faultCode = "ERR-003",
                            issueTitle = "Voltage Drop",
                            troubleshootingSteps = "Verify input voltage."
                        )
                    )
                    faultDao.insertAll(mockData)
                }
            }
        }).build()
    }

    @Provides
    @Singleton
    fun provideFaultDao(appDatabase: AppDatabase): FaultDao {
        return appDatabase.faultDao()
    }
}
