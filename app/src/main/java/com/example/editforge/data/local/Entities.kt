package com.example.editforge.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.editforge.data.model.JobStatus
import com.example.editforge.data.model.ProjectStatus
import com.example.editforge.data.model.StructureSection

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val projectName: String,
    val fileName: String,
    val fileSize: Long,
    val duration: Float,
    val status: String, // ProjectStatus name
    val expiresAt: Long,
    val coreBundleUnlocked: Boolean = false,
    val bundleAlternatesUsed: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "analyses")
data class AnalysisEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val bpm: Int,
    val musicKey: String,
    val genre: String,
    val mood: String,
    val energy: Float,
    val lufs: Float,
    val duration: Float,
    val structureJson: String,
    val waveformCsv: String
)

@Entity(tableName = "exports")
data class ExportEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val typeCode: String,
    val label: String,
    val duration: Float,
    val status: String, // JobStatus name
    val variation: Int = 1,
    val parentExportId: String? = null,
    val creditCost: Int = 2,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "credit_transactions")
data class CreditTransactionEntity(
    @PrimaryKey val id: String,
    val amount: Int,
    val reason: String,
    val description: String?,
    val balanceAfter: Int,
    val timestamp: Long = System.currentTimeMillis()
)
