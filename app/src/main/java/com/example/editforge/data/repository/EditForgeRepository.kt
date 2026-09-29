package com.example.editforge.data.repository

import com.example.editforge.data.local.*
import com.example.editforge.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class EditForgeRepository(private val database: EditForgeDatabase) {

    private val projectDao = database.projectDao()
    private val analysisDao = database.analysisDao()
    private val exportDao = database.exportDao()
    private val creditDao = database.creditDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultDataIfNeeded()
        }
    }

    val projectsFlow: Flow<List<Project>> = projectDao.getAllProjects().map { entities ->
        entities.map { it.toDomain() }
    }

    val allExportsFlow: Flow<List<ExportItem>> = exportDao.observeAllExports().map { entities ->
        entities.map { it.toDomain() }
    }

    val transactionsFlow: Flow<List<CreditTransaction>> = creditDao.observeTransactions().map { entities ->
        entities.map { it.toDomain() }
    }

    fun observeProject(projectId: String): Flow<Project?> =
        projectDao.observeProjectById(projectId).map { it?.toDomain() }

    fun observeAnalysis(projectId: String): Flow<AnalysisData?> =
        analysisDao.observeAnalysisForProject(projectId).map { it?.toDomain() }

    fun observeExports(projectId: String): Flow<List<ExportItem>> =
        exportDao.observeExportsForProject(projectId).map { list -> list.map { it.toDomain() } }

    suspend fun createProject(
        name: String,
        fileName: String,
        duration: Float = 214f,
        fileSize: Long = 14_250_000L
    ): Project {
        val id = UUID.randomUUID().toString()
        val retentionExpiry = System.currentTimeMillis() + (30L * 24 * 3600 * 1000) // 30 days
        val entity = ProjectEntity(
            id = id,
            projectName = name,
            fileName = fileName,
            fileSize = fileSize,
            duration = duration,
            status = ProjectStatus.UPLOADED.name,
            expiresAt = retentionExpiry,
            coreBundleUnlocked = false,
            bundleAlternatesUsed = 0,
            createdAt = System.currentTimeMillis()
        )
        projectDao.insertProject(entity)
        return entity.toDomain()
    }

    suspend fun runAnalysis(projectId: String, onProgress: (Float, String) -> Unit) {
        val project = projectDao.getProjectById(projectId) ?: return
        projectDao.updateProject(project.copy(status = ProjectStatus.ANALYZING.name))

        onProgress(0.2f, "Decoding audio master samples & spectrum...")
        delay(600)
        onProgress(0.45f, "Detecting transient peaks & tempo (BPM)...")
        delay(600)
        onProgress(0.70f, "Computing harmonic key profile & loudness (LUFS)...")
        delay(600)
        onProgress(0.90f, "Identifying musical boundaries (Intro, Verse, Chorus, Outro)...")
        delay(500)

        val duration = project.duration
        val sampleWaveform = generateRealisticWaveform(64)
        val sections = listOf(
            StructureSection("Intro", 0f, (duration * 0.12f), 0.94f),
            StructureSection("Verse", (duration * 0.12f), (duration * 0.38f), 0.91f),
            StructureSection("Chorus", (duration * 0.38f), (duration * 0.65f), 0.97f),
            StructureSection("Bridge", (duration * 0.65f), (duration * 0.82f), 0.88f),
            StructureSection("Outro", (duration * 0.82f), duration, 0.95f)
        )

        val analysisId = UUID.randomUUID().toString()
        val analysisEntity = AnalysisEntity(
            id = analysisId,
            projectId = projectId,
            bpm = 124,
            musicKey = "F Minor",
            genre = "Electronic / Synthwave",
            mood = "Energetic / Cinematic",
            energy = 0.86f,
            lufs = -13.8f,
            duration = duration,
            structureJson = serializeStructure(sections),
            waveformCsv = sampleWaveform.joinToString(",")
        )

        analysisDao.insertAnalysis(analysisEntity)
        projectDao.updateProject(project.copy(status = ProjectStatus.ANALYZED.name))
        onProgress(1.0f, "Analysis complete!")
    }

    suspend fun forgeCoreBundle(projectId: String): Boolean {
        val project = projectDao.getProjectById(projectId) ?: return false
        val types = listOf(
            DeliverableType.EDIT_60,
            DeliverableType.EDIT_30,
            DeliverableType.EDIT_15,
            DeliverableType.STING
        )

        deductCredits(5, "Core Bundle Forge", "Unlocked 60s, 30s, 15s + Sting package for ${project.projectName}")
        projectDao.updateProject(project.copy(status = ProjectStatus.GENERATING.name, coreBundleUnlocked = true))

        types.forEach { type ->
            val exportId = UUID.randomUUID().toString()
            exportDao.insertExport(
                ExportEntity(
                    id = exportId,
                    projectId = projectId,
                    typeCode = type.code,
                    label = type.title,
                    duration = type.defaultDuration,
                    status = JobStatus.PROCESSING.name,
                    variation = 1,
                    creditCost = type.creditCost
                )
            )
        }

        delay(1200)
        types.forEach { type ->
            val existing = exportDao.observeExportsForProject(projectId)
            // mark complete
        }

        val projectExports = exportDao.observeExportsForProject(projectId)
        // Update all to COMPLETE
        types.forEach { type ->
            exportDao.insertExport(
                ExportEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = projectId,
                    typeCode = type.code,
                    label = type.title,
                    duration = type.defaultDuration,
                    status = JobStatus.COMPLETE.name,
                    variation = 1,
                    creditCost = type.creditCost
                )
            )
        }

        projectDao.updateProject(project.copy(status = ProjectStatus.COMPLETE.name, coreBundleUnlocked = true))
        return true
    }

    suspend fun forgeSingleDeliverable(projectId: String, type: DeliverableType) {
        val project = projectDao.getProjectById(projectId) ?: return
        deductCredits(type.creditCost, "${type.title} Forge", "Rendered ${type.title} for ${project.projectName}")

        val exportId = UUID.randomUUID().toString()
        exportDao.insertExport(
            ExportEntity(
                id = exportId,
                projectId = projectId,
                typeCode = type.code,
                label = type.title,
                duration = type.defaultDuration,
                status = JobStatus.PROCESSING.name,
                variation = 1,
                creditCost = type.creditCost
            )
        )

        delay(800)
        exportDao.insertExport(
            ExportEntity(
                id = exportId,
                projectId = projectId,
                typeCode = type.code,
                label = type.title,
                duration = type.defaultDuration,
                status = JobStatus.COMPLETE.name,
                variation = 1,
                creditCost = type.creditCost
            )
        )
        projectDao.updateProject(project.copy(status = ProjectStatus.COMPLETE.name))
    }

    suspend fun forgeAlternative(projectId: String, originalExport: ExportItem) {
        val project = projectDao.getProjectById(projectId) ?: return
        val nextVariation = originalExport.variation + 1
        deductCredits(1, "Alternative Generation", "Variation #$nextVariation for ${originalExport.label}")

        val altId = UUID.randomUUID().toString()
        exportDao.insertExport(
            ExportEntity(
                id = altId,
                projectId = projectId,
                typeCode = originalExport.type.code,
                label = "${originalExport.type.title} (Var #$nextVariation)",
                duration = originalExport.duration,
                status = JobStatus.PROCESSING.name,
                variation = nextVariation,
                parentExportId = originalExport.id,
                creditCost = 1
            )
        )

        delay(900)
        exportDao.insertExport(
            ExportEntity(
                id = altId,
                projectId = projectId,
                typeCode = originalExport.type.code,
                label = "${originalExport.type.title} (Var #$nextVariation)",
                duration = originalExport.duration,
                status = JobStatus.COMPLETE.name,
                variation = nextVariation,
                parentExportId = originalExport.id,
                creditCost = 1
            )
        )
        projectDao.updateProject(
            project.copy(bundleAlternatesUsed = project.bundleAlternatesUsed + 1)
        )
    }

    suspend fun deleteProject(projectId: String) {
        exportDao.deleteExportsForProject(projectId)
        projectDao.deleteProjectById(projectId)
    }

    suspend fun deductCredits(amount: Int, reason: String, description: String?) {
        val currentBalance = getCurrentCreditBalance()
        val newBalance = (currentBalance - amount).coerceAtLeast(0)
        creditDao.insertTransaction(
            CreditTransactionEntity(
                id = UUID.randomUUID().toString(),
                amount = -amount,
                reason = reason,
                description = description,
                balanceAfter = newBalance,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun addCredits(amount: Int, reason: String, description: String? = null) {
        val currentBalance = getCurrentCreditBalance()
        val newBalance = currentBalance + amount
        creditDao.insertTransaction(
            CreditTransactionEntity(
                id = UUID.randomUUID().toString(),
                amount = amount,
                reason = reason,
                description = description,
                balanceAfter = newBalance,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private var cachedBalance: Int = 35

    suspend fun getCurrentCreditBalance(): Int {
        return cachedBalance
    }

    private suspend fun seedDefaultDataIfNeeded() {
        val project1Id = "demo-project-neon-horizons"
        val existing = projectDao.getProjectById(project1Id)
        if (existing == null) {
            val now = System.currentTimeMillis()
            val expiry = now + (28L * 24 * 3600 * 1000) // 28 days left

            projectDao.insertProject(
                ProjectEntity(
                    id = project1Id,
                    projectName = "Neon Horizons - Studio Master",
                    fileName = "neon_horizons_master_24bit_48k.wav",
                    fileSize = 42_500_000L,
                    duration = 214.5f,
                    status = ProjectStatus.COMPLETE.name,
                    expiresAt = expiry,
                    coreBundleUnlocked = true,
                    bundleAlternatesUsed = 1,
                    createdAt = now - (2L * 24 * 3600 * 1000)
                )
            )

            val structure = listOf(
                StructureSection("Intro", 0f, 24.5f, 0.95f),
                StructureSection("Verse", 24.5f, 78.0f, 0.92f),
                StructureSection("Chorus", 78.0f, 134.5f, 0.98f),
                StructureSection("Bridge", 134.5f, 172.0f, 0.89f),
                StructureSection("Outro", 172.0f, 214.5f, 0.96f)
            )

            analysisDao.insertAnalysis(
                AnalysisEntity(
                    id = "demo-analysis-1",
                    projectId = project1Id,
                    bpm = 126,
                    musicKey = "F# Minor",
                    genre = "Synthwave / Cinematic",
                    mood = "Driving & Atmospheric",
                    energy = 0.88f,
                    lufs = -13.9f,
                    duration = 214.5f,
                    structureJson = serializeStructure(structure),
                    waveformCsv = AudioDemosData.DEMOS[0].peaks.joinToString(",")
                )
            )

            // Seed exports for project 1
            exportDao.insertExport(
                ExportEntity(
                    id = "exp-1",
                    projectId = project1Id,
                    typeCode = DeliverableType.EDIT_60.code,
                    label = "60s Radio Edit",
                    duration = 60.5f,
                    status = JobStatus.COMPLETE.name,
                    variation = 1,
                    creditCost = 2
                )
            )
            exportDao.insertExport(
                ExportEntity(
                    id = "exp-2",
                    projectId = project1Id,
                    typeCode = DeliverableType.EDIT_30.code,
                    label = "30s Hook Edit",
                    duration = 30.5f,
                    status = JobStatus.COMPLETE.name,
                    variation = 1,
                    creditCost = 2
                )
            )
            exportDao.insertExport(
                ExportEntity(
                    id = "exp-3",
                    projectId = project1Id,
                    typeCode = DeliverableType.EDIT_15.code,
                    label = "15s Social Edit",
                    duration = 15.4f,
                    status = JobStatus.COMPLETE.name,
                    variation = 1,
                    creditCost = 2
                )
            )
            exportDao.insertExport(
                ExportEntity(
                    id = "exp-4",
                    projectId = project1Id,
                    typeCode = DeliverableType.STING.code,
                    label = "Sting (5s)",
                    duration = 5.0f,
                    status = JobStatus.COMPLETE.name,
                    variation = 1,
                    creditCost = 1
                )
            )

            // Initial credit grant
            creditDao.insertTransaction(
                CreditTransactionEntity(
                    id = "tx-welcome",
                    amount = 50,
                    reason = "Studio Welcome Grant",
                    description = "Monthly Pro Studio credit allocation",
                    balanceAfter = 50,
                    timestamp = now - (3L * 24 * 3600 * 1000)
                )
            )
            creditDao.insertTransaction(
                CreditTransactionEntity(
                    id = "tx-core-bundle",
                    amount = -5,
                    reason = "Core Bundle Forge",
                    description = "Unlocked 60s, 30s, 15s + Sting package for Neon Horizons",
                    balanceAfter = 45,
                    timestamp = now - (2L * 24 * 3600 * 1000)
                )
            )
            creditDao.insertTransaction(
                CreditTransactionEntity(
                    id = "tx-alt-cut",
                    amount = -1,
                    reason = "Alternative Generation",
                    description = "Variation #2 for 30s Hook Edit",
                    balanceAfter = 44,
                    timestamp = now - (1L * 24 * 3600 * 1000)
                )
            )
            cachedBalance = 44
        }
    }

    private fun generateRealisticWaveform(count: Int): List<Float> {
        return List(count) { i ->
            val phase = (i.toFloat() / count) * Math.PI.toFloat() * 4
            val base = (Math.sin(phase.toDouble()).toFloat() * 0.4f + 0.5f)
            (base * (0.6f + (i % 7) * 0.05f)).coerceIn(0.2f, 0.98f)
        }
    }

    private fun serializeStructure(sections: List<StructureSection>): String {
        return sections.joinToString(";") { "${it.label}|${it.start}|${it.end}|${it.confidence}" }
    }
}

fun ProjectEntity.toDomain(): Project = Project(
    id = id,
    projectName = projectName,
    fileName = fileName,
    fileSize = fileSize,
    duration = duration,
    status = try { ProjectStatus.valueOf(status) } catch (e: Exception) { ProjectStatus.UPLOADED },
    expiresAt = expiresAt,
    coreBundleUnlocked = coreBundleUnlocked,
    bundleAlternatesUsed = bundleAlternatesUsed,
    createdAt = createdAt
)

fun ExportEntity.toDomain(): ExportItem = ExportItem(
    id = id,
    projectId = projectId,
    type = DeliverableType.fromCode(typeCode),
    label = label,
    duration = duration,
    status = try { JobStatus.valueOf(status) } catch (e: Exception) { JobStatus.COMPLETE },
    variation = variation,
    parentExportId = parentExportId,
    creditCost = creditCost,
    createdAt = createdAt
)

fun AnalysisEntity.toDomain(): AnalysisData {
    val sections = structureJson.split(";").filter { it.isNotBlank() }.map { part ->
        val sub = part.split("|")
        StructureSection(
            label = sub.getOrNull(0) ?: "Section",
            start = sub.getOrNull(1)?.toFloatOrNull() ?: 0f,
            end = sub.getOrNull(2)?.toFloatOrNull() ?: 30f,
            confidence = sub.getOrNull(3)?.toFloatOrNull() ?: 0.9f
        )
    }
    val wave = waveformCsv.split(",").mapNotNull { it.toFloatOrNull() }
    return AnalysisData(
        id = id,
        projectId = projectId,
        bpm = bpm,
        musicKey = musicKey,
        genre = genre,
        mood = mood,
        energy = energy,
        lufs = lufs,
        duration = duration,
        structure = sections,
        waveform = wave
    )
}

fun CreditTransactionEntity.toDomain(): CreditTransaction = CreditTransaction(
    id = id,
    amount = amount,
    reason = reason,
    description = description,
    balanceAfter = balanceAfter,
    timestamp = timestamp
)
