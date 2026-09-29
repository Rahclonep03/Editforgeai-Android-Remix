package com.example.editforge.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeProjectById(id: String): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analyses WHERE projectId = :projectId LIMIT 1")
    fun observeAnalysisForProject(projectId: String): Flow<AnalysisEntity?>

    @Query("SELECT * FROM analyses WHERE projectId = :projectId LIMIT 1")
    suspend fun getAnalysisForProject(projectId: String): AnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisEntity)
}

@Dao
interface ExportDao {
    @Query("SELECT * FROM exports WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun observeExportsForProject(projectId: String): Flow<List<ExportEntity>>

    @Query("SELECT * FROM exports ORDER BY createdAt DESC")
    fun observeAllExports(): Flow<List<ExportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExport(export: ExportEntity)

    @Update
    suspend fun updateExport(export: ExportEntity)

    @Query("DELETE FROM exports WHERE projectId = :projectId")
    suspend fun deleteExportsForProject(projectId: String)
}

@Dao
interface CreditDao {
    @Query("SELECT * FROM credit_transactions ORDER BY timestamp DESC")
    fun observeTransactions(): Flow<List<CreditTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: CreditTransactionEntity)
}
