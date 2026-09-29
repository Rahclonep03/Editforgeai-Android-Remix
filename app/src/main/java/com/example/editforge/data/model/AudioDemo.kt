package com.example.editforge.data.model

data class AudioDemo(
    val id: String,
    val title: String,
    val duration: Float,
    val durationLabel: String,
    val copy: String,
    val peaks: List<Float>,
    val badge: String = "Deliverable"
)

object AudioDemosData {
    val MASTER_TRACK = AudioDemo(
        id = "demo-project-cybernetic-groove",
        title = "Cybernetic Groove.wav (Original Master)",
        duration = 150.0f,
        durationLabel = "2:30",
        copy = "Full 24-bit / 48kHz studio master track. Features an energetic 122 BPM cybernetic synth-funk groove, punchy slap-bassline, polyphonic analog synth chords, and vocoder transitions.",
        badge = "Master WAV",
        peaks = listOf(
            0.62f, 0.74f, 0.81f, 0.89f, 0.95f, 0.92f, 0.86f, 0.91f,
            0.78f, 0.85f, 0.88f, 0.94f, 0.97f, 0.89f, 0.84f, 0.92f,
            0.76f, 0.88f, 0.91f, 0.96f, 0.93f, 0.87f, 0.90f, 0.85f,
            0.79f, 0.83f, 0.89f, 0.95f, 0.98f, 0.91f, 0.86f, 0.93f,
            0.68f, 0.75f, 0.82f, 0.88f, 0.92f, 0.87f, 0.79f, 0.84f,
            0.88f, 0.94f, 0.96f, 0.99f, 0.95f, 0.91f, 0.88f, 0.93f,
            0.82f, 0.87f, 0.92f, 0.96f, 0.94f, 0.89f, 0.85f, 0.91f,
            0.77f, 0.84f, 0.88f, 0.93f, 0.90f, 0.83f, 0.72f, 0.55f
        )
    )

    val DEMOS = listOf(
        AudioDemo(
            id = "edit-60",
            title = "Cybernetic Groove → 60-Second Radio Edit",
            duration = 60.0f,
            durationLabel = "1:00",
            copy = "Preserves the cybernetic funk identity, transitions directly from the intro build into the main bass hook, and resolves at a clean musical cadence instead of fading out.",
            badge = "60s Cut",
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
            title = "Cybernetic Groove → 30-Second Hook Edit",
            duration = 30.0f,
            durationLabel = "0:30",
            copy = "Distills the prime cybernetic slap-bass riff and vocal chops for immediate commercial punch, short-form video hooks, and podcast title sequences.",
            badge = "30s Cut",
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
            title = "Cybernetic Groove → 15-Second Social Reel Cut",
            duration = 15.0f,
            durationLabel = "0:15",
            copy = "Zero wasted seconds: drops directly on the driving cybernetic drop with razor-sharp transient peaks, concluding on an intentional bass-stinger hit.",
            badge = "15s Cut",
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
        ),
        AudioDemo(
            id = "edit-sting",
            title = "Cybernetic Groove → 5-Second Stinger Ident",
            duration = 5.0f,
            durationLabel = "0:05",
            copy = "Broadcast-ready audio logo ident crafted from the closing synthesizer resolution and robotic bass punch of Cybernetic Groove.wav.",
            badge = "5s Sting",
            peaks = listOf(
                0.95f, 0.98f, 0.92f, 0.88f, 0.82f, 0.74f, 0.65f, 0.58f,
                0.50f, 0.42f, 0.35f, 0.28f, 0.20f, 0.15f, 0.10f, 0.05f
            )
        ),
        AudioDemo(
            id = "edit-stems",
            title = "Cybernetic Groove → Demucs Stems (Bass + Drums)",
            duration = 30.0f,
            durationLabel = "0:30",
            copy = "Neural stem separation isolating the funky slap-bass line and electronic drum rhythm with zero harmonic bleed.",
            badge = "Demucs Stems",
            peaks = listOf(
                0.82f, 0.75f, 0.88f, 0.94f, 0.90f, 0.83f, 0.89f, 0.85f,
                0.78f, 0.84f, 0.91f, 0.95f, 0.89f, 0.82f, 0.87f, 0.81f,
                0.85f, 0.90f, 0.93f, 0.88f, 0.82f, 0.86f, 0.89f, 0.92f,
                0.76f, 0.83f, 0.89f, 0.94f, 0.91f, 0.85f, 0.78f, 0.65f
            )
        )
    )
}
