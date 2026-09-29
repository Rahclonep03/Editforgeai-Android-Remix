package com.example.editforge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        AnalysisEntity::class,
        ExportEntity::class,
        CreditTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EditForgeDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun analysisDao(): AnalysisDao
    abstract fun exportDao(): ExportDao
    abstract fun creditDao(): CreditDao

    companion object {
        @Volatile
        private var INSTANCE: EditForgeDatabase? = null

        fun getInstance(context: Context): EditForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EditForgeDatabase::class.java,
                    "editforge_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
