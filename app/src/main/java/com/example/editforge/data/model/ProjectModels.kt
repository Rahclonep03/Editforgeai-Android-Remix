package com.example.editforge.data.model

enum class ProjectStatus(val label: String) {
    UPLOADED("Uploaded"),
    ANALYZING("Analyzing"),
    ANALYZED("Analyzed"),
    GENERATING("Generating"),
    COMPLETE("Complete"),
    FAILED("Failed")
}

enum class JobStatus(val label: String) {
    QUEUED("Queued"),
    PROCESSING("Processing"),
    COMPLETE("Complete"),
    FAILED("Failed")
}

enum class DeliverableType(
    val code: String,
    val title: String,
    val description: String,
    val defaultDuration: Float,
    val creditCost: Int
) {
    FULL_MIX("full_mix", "Full Mix", "Mastered full-length master", 214f, 1),
    EDIT_60("edit_60", "60s Radio Edit", "Broadcast & streaming cut-down", 60.5f, 2),
    EDIT_30("edit_30", "30s Hook Edit", "Ad-length hook-first commercial cut", 30.5f, 2),
    EDIT_15("edit_15", "15s Social Edit", "Short-form vertical reel/TikTok edit", 15.4f, 2),
    STING("sting", "Sting", "3-6s branded audio ident & logo bumper", 5.0f, 1),
    CUSTOM("custom", "Custom Edit", "User-specified in/out boundaries", 45.0f, 2),
    STEM_VOCALS("stem_vocals", "Vocals Stem", "AI-separated lead & harmony vocals", 214f, 3),
    STEM_DRUMS("stem_drums", "Drums Stem", "AI-separated drum kit & percussion", 214f, 3),
    STEM_BASS("stem_bass", "Bass Stem", "AI-separated bass guitar / sub synthesizer", 214f, 3),
    STEM_OTHER("stem_other", "Other Stem", "Instruments, synthesizers & FX", 214f, 3);

    companion object {
        fun fromCode(code: String): DeliverableType {
            return entries.firstOrNull { it.code == code } ?: EDIT_60
        }
    }
}

data class StructureSection(
    val label: String, // "Intro", "Verse", "Chorus", "Bridge", "Outro"
    val start: Float,
    val end: Float,
    val confidence: Float
)

data class Project(
    val id: String,
    val projectName: String,
    val fileName: String,
    val fileSize: Long,
    val duration: Float,
    val status: ProjectStatus,
    val expiresAt: Long, // timestamp millis (30-day retention)
    val coreBundleUnlocked: Boolean = false,
    val bundleAlternatesUsed: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class AnalysisData(
    val id: String,
    val projectId: String,
    val bpm: Int,
    val musicKey: String,
    val genre: String,
    val mood: String,
    val energy: Float, // 0.0 - 1.0
    val lufs: Float,   // e.g. -14.2
    val duration: Float,
    val structure: List<StructureSection>,
    val waveform: List<Float>
)

data class ExportItem(
    val id: String,
    val projectId: String,
    val type: DeliverableType,
    val label: String,
    val duration: Float,
    val status: JobStatus,
    val variation: Int = 1,
    val parentExportId: String? = null,
    val creditCost: Int = 2,
    val createdAt: Long = System.currentTimeMillis()
)

data class CreditTransaction(
    val id: String,
    val amount: Int,
    val reason: String,
    val description: String?,
    val balanceAfter: Int,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SubscriptionTier(
    val label: String,
    val monthlyCredits: Int,
    val price: String,
    val monthlyAmount: Double,
    val annualPriceFormatted: String,
    val annualAmount: Double
) {
    FREE("Free", 5, "$0", 0.0, "$0", 0.0),
    PLUS("Plus", 50, "$11.99/mo", 11.99, "$115.00/yr", 115.0),
    PRO("Pro Studio", 150, "$21.99/mo", 21.99, "$211.00/yr", 211.0),
    PREMIER("Premier", 500, "$79.99/mo", 79.99, "$767.00/yr", 767.0)
}
