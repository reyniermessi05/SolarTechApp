package com.example.solarapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FaultCode::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun faultDao(): FaultDao
}
