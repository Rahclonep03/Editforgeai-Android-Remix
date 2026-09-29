package com.example.editforge.data.model

data class AudioDemo(
    val id: String,
    val title: String,
    val duration: Float,
    val durationLabel: String,
    val copy: String,
    val peaks: List<Float>
)

object AudioDemosData {
    val DEMOS = listOf(
        AudioDemo(
            id = "edit-60",
            title = "Full master → 60-second radio edit",
            duration = 60.5f,
            durationLabel = "1:00",
            copy = "Keeps the song's identity, reaches the hook early, and resolves at a musical boundary instead of fading out.",
            peaks = listOf(
                0.899f, 0.854f, 0.701f, 0.905f, 0.844f, 0.81f, 0.877f, 0.818f,
                0.731f, 0.764f, 0.772f, 0.895f, 0.61f, 0.697f, 0.906f, 0.797f,
                0.88f, 0.748f, 0.791f, 0.806f, 0.887f, 0.83f, 0.794f, 0.865f,
                0.727f, 0.773f, 0.833f, 0.839f, 0.95f, 0.882f, 0.766f, 0.824f,
                0.822f, 0.896f, 0.849f, 0.838f, 0.744f, 0.867f, 0.742f, 0.858f,
                0.791f, 0.737f, 0.715f, 0.787f, 0.817f, 0.828f, 0.863f, 0.72f,
                0.8f, 0.798f, 0.597f, 0.829f, 0.861f, 0.806f, 0.657f, 0.795f,
                0.739f, 0.838f, 0.826f, 0.78f, 0.79f, 0.757f, 0.814f, 0.873f
            )
        ),
        AudioDemo(
            id = "edit-30",
            title = "Full master → 30-second hook-first edit",
            duration = 30.5f,
            durationLabel = "0:30",
            copy = "Prioritises the strongest hook of the same master and drops the sections that do not serve a shorter format.",
            peaks = listOf(
                0.899f, 0.541f, 0.854f, 0.399f, 0.701f, 0.668f, 0.844f, 0.905f,
                0.805f, 0.844f, 0.664f, 0.81f, 0.421f, 0.877f, 0.818f, 0.534f,
                0.731f, 0.53f, 0.764f, 0.641f, 0.693f, 0.772f, 0.788f, 0.895f,
                0.608f, 0.61f, 0.579f, 0.906f, 0.779f, 0.803f, 0.797f, 0.748f,
                0.88f, 0.708f, 0.748f, 0.472f, 0.791f, 0.747f, 0.765f, 0.806f,
                0.887f, 0.745f, 0.83f, 0.785f, 0.794f, 0.579f, 0.865f, 0.552f,
                0.727f, 0.773f, 0.634f, 0.833f, 0.762f, 0.813f, 0.529f, 0.839f,
                0.784f, 0.95f, 0.817f, 0.756f, 0.766f, 0.635f, 0.824f, 0.495f
            )
        ),
        AudioDemo(
            id = "edit-15",
            title = "Full master → 15-second social edit",
            duration = 15.4f,
            durationLabel = "0:15",
            copy = "Builds a compact moment around the most recognisable material, with a clean musical end rather than an automatic fade.",
            peaks = listOf(
                0.942f, 0.782f, 0.561f, 0.568f, 0.588f, 0.895f, 0.419f, 0.411f,
                0.735f, 0.642f, 0.7f, 0.885f, 0.877f, 0.677f, 0.949f, 0.923f,
                0.803f, 0.844f, 0.885f, 0.793f, 0.693f, 0.62f, 0.606f, 0.849f,
                0.442f, 0.33f, 0.919f, 0.858f, 0.806f, 0.531f, 0.56f, 0.44f,
                0.766f, 0.573f, 0.556f, 0.554f, 0.801f, 0.783f, 0.443f, 0.47f,
                0.614f, 0.81f, 0.443f, 0.365f, 0.826f, 0.938f, 0.776f, 0.594f,
                0.638f, 0.526f, 0.64f, 0.572f, 0.607f, 0.731f, 0.95f, 0.816f,
                0.815f, 0.842f, 0.767f, 0.836f, 0.767f, 0.68f, 0.45f, 0.785f
            )
        )
    )
}
