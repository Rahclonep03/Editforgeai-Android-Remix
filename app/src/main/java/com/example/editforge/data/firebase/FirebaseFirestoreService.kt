package com.example.editforge.data.firebase

import android.util.Log
import com.example.editforge.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FirebaseFirestoreService {

    private val firestore: FirebaseFirestore by lazy {
        val db = FirebaseFirestore.getInstance()
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w("FirestoreService", "Settings already initialized or offline cache fallback active: ${e.message}")
        }
        db
    }

    suspend fun syncUserProfile(
        userId: String,
        creditBalance: Int,
        email: String?,
        displayName: String?
    ) {
        try {
            val userDoc = firestore.collection("users").document(userId)
            val data = hashMapOf(
                "uid" to userId,
                "email" to (email ?: "anonymous@editforge.studio"),
                "displayName" to (displayName ?: "Studio Producer"),
                "creditBalance" to creditBalance,
                "lastActive" to System.currentTimeMillis()
            )
            withTimeoutOrNull(2000L) {
                userDoc.set(data, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error syncing user profile: ${e.message}")
        }
    }

    suspend fun syncProjectToCloud(userId: String, project: Project) {
        try {
            val projectDoc = firestore.collection("users")
                .document(userId)
                .collection("projects")
                .document(project.id)

            val data = hashMapOf(
                "id" to project.id,
                "projectName" to project.projectName,
                "fileName" to project.fileName,
                "fileSize" to project.fileSize,
                "duration" to project.duration,
                "status" to project.status.name,
                "expiresAt" to project.expiresAt,
                "coreBundleUnlocked" to project.coreBundleUnlocked,
                "bundleAlternatesUsed" to project.bundleAlternatesUsed,
                "createdAt" to project.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            withTimeoutOrNull(2000L) {
                projectDoc.set(data, SetOptions.merge()).await()
            }
            Log.d("FirestoreService", "Synced project ${project.projectName} to Firestore for user $userId")
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error syncing project to Firestore: ${e.message}")
        }
    }

    suspend fun deleteProjectFromCloud(userId: String, projectId: String) {
        try {
            withTimeoutOrNull(2000L) {
                firestore.collection("users")
                    .document(userId)
                    .collection("projects")
                    .document(projectId)
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error deleting project from Firestore: ${e.message}")
        }
    }

    suspend fun syncExportToCloud(userId: String, export: ExportItem) {
        try {
            val exportDoc = firestore.collection("users")
                .document(userId)
                .collection("exports")
                .document(export.id)

            val data = hashMapOf(
                "id" to export.id,
                "projectId" to export.projectId,
                "type" to export.type.name,
                "label" to export.label,
                "duration" to export.duration,
                "status" to export.status.name,
                "variation" to export.variation,
                "parentExportId" to export.parentExportId,
                "creditCost" to export.creditCost,
                "createdAt" to export.createdAt,
                "syncedAt" to System.currentTimeMillis()
            )
            withTimeoutOrNull(2000L) {
                exportDoc.set(data, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error syncing export to Firestore: ${e.message}")
        }
    }

    suspend fun fetchCloudProjects(userId: String): List<Project> {
        return try {
            val snapshot = withTimeoutOrNull(3000L) {
                firestore.collection("users")
                    .document(userId)
                    .collection("projects")
                    .get()
                    .await()
            }

            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    Project(
                        id = doc.getString("id") ?: doc.id,
                        projectName = doc.getString("projectName") ?: "Cloud Track",
                        fileName = doc.getString("fileName") ?: "master.wav",
                        fileSize = doc.getLong("fileSize") ?: 12_000_000L,
                        duration = doc.getDouble("duration")?.toFloat() ?: 180f,
                        status = try {
                            ProjectStatus.valueOf(doc.getString("status") ?: ProjectStatus.COMPLETE.name)
                        } catch (e: Exception) {
                            ProjectStatus.COMPLETE
                        },
                        expiresAt = doc.getLong("expiresAt") ?: (System.currentTimeMillis() + 30L * 86400000L),
                        coreBundleUnlocked = doc.getBoolean("coreBundleUnlocked") ?: false,
                        bundleAlternatesUsed = doc.getLong("bundleAlternatesUsed")?.toInt() ?: 0,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            if (list.isNotEmpty()) {
                list
            } else {
                // If cloud projects collection is empty for this user, seed default active audio editing projects
                val seeded = getSeedActiveProjects()
                seeded.forEach { proj ->
                    syncProjectToCloud(userId, proj)
                }
                seeded
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error fetching projects from Firestore: ${e.message}")
            getSeedActiveProjects()
        }
    }

    fun getSeedActiveProjects(): List<Project> {
        val now = System.currentTimeMillis()
        val expiry = now + (28L * 86400000L)
        return listOf(
            Project(
                id = "demo-project-cybernetic-groove",
                projectName = "Cybernetic Groove - Studio Master",
                fileName = "Cybernetic Groove.wav",
                fileSize = 44_100_000L,
                duration = 150.0f,
                status = ProjectStatus.COMPLETE,
                expiresAt = expiry,
                coreBundleUnlocked = true,
                bundleAlternatesUsed = 1,
                createdAt = now - (2L * 86400000L)
            ),
            Project(
                id = "proj-cyber-city-drive",
                projectName = "Cyber City Drive - Stems Separation",
                fileName = "cyber_city_drive_stems_master.wav",
                fileSize = 38_200_000L,
                duration = 188.0f,
                status = ProjectStatus.GENERATING,
                expiresAt = expiry + (2L * 86400000L),
                coreBundleUnlocked = false,
                bundleAlternatesUsed = 0,
                createdAt = now - (12L * 3600000L)
            ),
            Project(
                id = "proj-midnight-echoes",
                projectName = "Midnight Echoes - Master Cut-Down",
                fileName = "midnight_echoes_rough_mix.mp3",
                fileSize = 18_600_000L,
                duration = 165.2f,
                status = ProjectStatus.ANALYZING,
                expiresAt = expiry - (5L * 86400000L),
                coreBundleUnlocked = false,
                bundleAlternatesUsed = 0,
                createdAt = now - (3L * 3600000L)
            ),
            Project(
                id = "proj-retro-waves",
                projectName = "Retro Waves - Radio Edit Forge",
                fileName = "retro_waves_full_session.wav",
                fileSize = 35_100_000L,
                duration = 198.0f,
                status = ProjectStatus.ANALYZED,
                expiresAt = expiry - (1L * 86400000L),
                coreBundleUnlocked = true,
                bundleAlternatesUsed = 2,
                createdAt = now - (1L * 86400000L)
            ),
            Project(
                id = "proj-starlight-synth",
                projectName = "Starlight Synth - 15s/30s Social Cuts",
                fileName = "starlight_synth_v2.wav",
                fileSize = 29_400_000L,
                duration = 142.0f,
                status = ProjectStatus.COMPLETE,
                expiresAt = expiry - (4L * 86400000L),
                coreBundleUnlocked = false,
                bundleAlternatesUsed = 0,
                createdAt = now - (4L * 86400000L)
            )
        )
    }

    fun listenToCloudProjects(
        userId: String,
        onUpdate: (List<Project>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {
        return firestore.collection("users")
            .document(userId)
            .collection("projects")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val projects = snapshot.documents.mapNotNull { doc ->
                        try {
                            Project(
                                id = doc.getString("id") ?: doc.id,
                                projectName = doc.getString("projectName") ?: "Cloud Track",
                                fileName = doc.getString("fileName") ?: "master.wav",
                                fileSize = doc.getLong("fileSize") ?: 12_000_000L,
                                duration = doc.getDouble("duration")?.toFloat() ?: 180f,
                                status = try {
                                    ProjectStatus.valueOf(doc.getString("status") ?: ProjectStatus.COMPLETE.name)
                                } catch (e: Exception) {
                                    ProjectStatus.COMPLETE
                                },
                                expiresAt = doc.getLong("expiresAt") ?: (System.currentTimeMillis() + 30L * 86400000L),
                                coreBundleUnlocked = doc.getBoolean("coreBundleUnlocked") ?: false,
                                bundleAlternatesUsed = doc.getLong("bundleAlternatesUsed")?.toInt() ?: 0,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    onUpdate(projects)
                }
            }
    }
}
