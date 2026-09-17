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
                    val mockData = listOf(
                        FaultCode(
                            equipmentType = "HEM Gen. 2",
                            faultCode = "F1: Watchdog",
                            issueTitle = "DSP Fault / Defective software update",
                            troubleshootingSteps = "1. Disconnect for ten seconds and reconnect the auxiliary services supply.\n2. Reinstall the microcontroller and initialize the Service Group.\n[Enlace: manual_hem2.pdf, Pag: 45]",
                            issueTitleEs = "Fallo DSP / Actualización de software defectuosa",
                            troubleshootingStepsEs = "1. Desconectar durante diez segundos y volver a conectar el suministro de servicios auxiliares.\n2. Reinstalar el microcontrolador e inicializar el Grupo de Servicio.\n[Enlace: manual_hem2.pdf, Pag: 45]"
                        ),
                        FaultCode(
                            equipmentType = "HEM Gen. 2",
                            faultCode = "F5: High Vac",
                            issueTitle = "High AC voltage fault",
                            troubleshootingSteps = "1. Check the voltage with an external voltmeter and the displayed voltage in the inverter.\n2. Check the AC voltage measurement fuses.\n[Enlace: manual_hem2.pdf, Pag: 46]",
                            issueTitleEs = "Fallo de alto voltaje AC",
                            troubleshootingStepsEs = "1. Compruebe el voltaje con un voltímetro externo y el voltaje mostrado en el inversor.\n2. Compruebe los fusibles de medición de voltaje AC.\n[Enlace: manual_hem2.pdf, Pag: 46]"
                        ),
                        FaultCode(
                            equipmentType = "HEM Gen. 2",
                            faultCode = "F65: Mod. AC overcurrent",
                            issueTitle = "Overcurrent in any of the three phases",
                            troubleshootingSteps = "1. Check wiring, including optical fiber.\n2. Check that the AC fuses are not blown.\n[Enlace: diagrama_hem2.pdf, Pag: 10]",
                            issueTitleEs = "Sobrecorriente en cualquiera de las tres fases",
                            troubleshootingStepsEs = "1. Compruebe el cableado, incluida la fibra óptica.\n2. Compruebe que los fusibles AC no estén fundidos.\n[Enlace: diagrama_hem2.pdf, Pag: 10]"
                        )
                    )
                    faultDao.insertAll(mockData)
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
