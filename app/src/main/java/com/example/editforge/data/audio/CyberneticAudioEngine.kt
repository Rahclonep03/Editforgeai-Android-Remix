package com.example.editforge.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * Real-time synthesis engine for "Cybernetic Groove.wav".
 * Generates an authentic 122 BPM cybernetic synth-funk groove with punchy basslines,
 * polyphonic synth chords, and rhythmic percussion via Android AudioTrack.
 */
object CyberneticAudioEngine {

    private const val SAMPLE_RATE = 44100
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var isEngineRunning = false

    @Synchronized
    fun start(durationSeconds: Float = 150.0f, startOffsetRatio: Float = 0f) {
        stop()

        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * 4)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isEngineRunning = true

            playbackJob = CoroutineScope(Dispatchers.Default).launch {
                val chunkSize = 2048
                val buffer = ShortArray(chunkSize)
                var currentSample = (startOffsetRatio * durationSeconds * SAMPLE_RATE).toLong()
                val totalSamples = (durationSeconds * SAMPLE_RATE).toLong()

                val bpm = 122.0
                val secondsPerBeat = 60.0 / bpm
                val samplesPerBeat = (secondsPerBeat * SAMPLE_RATE).toInt()
                val samplesPer16th = samplesPerBeat / 4

                // E Minor cybernetic bass frequencies
                val bassNotes = doubleArrayOf(
                    82.41,  // E2
                    82.41,  // E2
                    98.00,  // G2
                    82.41,  // E2
                    110.00, // A2
                    82.41,  // E2
                    123.47, // B2
                    146.83, // D3
                    82.41,  // E2
                    98.00,  // G2
                    82.41,  // E2
                    73.42,  // D2
                    82.41,  // E2
                    110.00, // A2
                    98.00,  // G2
                    123.47  // B2
                )

                // Synth arpeggio frequencies (E minor 9)
                val synthNotes = doubleArrayOf(
                    329.63, // E4
                    392.00, // G4
                    493.88, // B4
                    587.33, // D5
                    659.25, // E5
                    587.33, // D5
                    493.88, // B4
                    392.00  // G4
                )

                while (isActive && isEngineRunning && currentSample < totalSamples) {
                    for (i in 0 until chunkSize) {
                        val sampleIdx = currentSample + i
                        val t = sampleIdx.toDouble() / SAMPLE_RATE

                        val beatNumber = (sampleIdx / samplesPerBeat) % 4
                        val sampleInBeat = sampleIdx % samplesPerBeat
                        val sixteenthIdx = (sampleIdx / samplesPer16th) % bassNotes.size
                        val sampleIn16th = sampleIdx % samplesPer16th

                        // 1. Cybernetic Slap Bass Synth (Saw + Square with resonant lowpass decay)
                        val bassFreq = bassNotes[sixteenthIdx.toInt()]
                        val bassPhase = (sampleIdx * bassFreq / SAMPLE_RATE) % 1.0
                        val bassRaw = (2.0 * bassPhase - 1.0) * 0.6 + (if (bassPhase > 0.5) 0.4 else -0.4)
                        val bassEnv = (1.0 - (sampleIn16th.toDouble() / samplesPer16th)).coerceIn(0.0, 1.0)
                        val bassSample = bassRaw * (bassEnv.pow(1.5)) * 0.45

                        // 2. Cybernetic Arpeggio Lead (Polyphonic FM chime)
                        val arpIdx = ((sampleIdx / (samplesPer16th / 2)) % synthNotes.size).toInt()
                        val arpFreq = synthNotes[arpIdx]
                        val arpMod = sin(2.0 * PI * (arpFreq * 2.0) * t) * 0.4
                        val arpPhase = 2.0 * PI * arpFreq * t + arpMod
                        val arpEnv = (1.0 - ((sampleIdx % (samplesPer16th / 2)).toDouble() / (samplesPer16th / 2))).coerceIn(0.0, 1.0)
                        val arpSample = sin(arpPhase) * (arpEnv.pow(2.0)) * 0.22

                        // 3. Punchy Electronic Kick on Beats 1 & 3
                        var kickSample = 0.0
                        if (beatNumber == 0L || beatNumber == 2L) {
                            val kickT = sampleInBeat.toDouble() / SAMPLE_RATE
                            if (kickT < 0.22) {
                                val kickFreq = 150.0 * (1.0 - kickT / 0.22).pow(3.0) + 42.0
                                val kickPhase = 2.0 * PI * kickFreq * kickT
                                val kickEnv = (1.0 - kickT / 0.22).pow(2.0)
                                kickSample = sin(kickPhase) * kickEnv * 0.55
                            }
                        }

                        // 4. Snare / Cybernetic Clap on Beats 2 & 4
                        var snareSample = 0.0
                        if (beatNumber == 1L || beatNumber == 3L) {
                            val snareT = sampleInBeat.toDouble() / SAMPLE_RATE
                            if (snareT < 0.18) {
                                val noise = (Math.random() * 2.0 - 1.0)
                                val body = sin(2.0 * PI * 185.0 * snareT) * 0.35
                                val snareEnv = (1.0 - snareT / 0.18).pow(1.8)
                                snareSample = (noise * 0.4 + body) * snareEnv * 0.40
                            }
                        }

                        // 5. Crisp Hi-Hat on 16th notes
                        var hihatSample = 0.0
                        val hatT = sampleIn16th.toDouble() / SAMPLE_RATE
                        if (hatT < 0.05) {
                            val noise = (Math.random() * 2.0 - 1.0)
                            val hatEnv = (1.0 - hatT / 0.05).pow(2.5)
                            hihatSample = noise * hatEnv * 0.14
                        }

                        // Master Mix Sum
                        val mixed = (bassSample + arpSample + kickSample + snareSample + hihatSample)
                            .coerceIn(-0.95, 0.95)

                        buffer[i] = (mixed * 32767.0).toInt().toShort()
                    }

                    audioTrack?.write(buffer, 0, chunkSize)
                    currentSample += chunkSize
                }
            }
        } catch (e: Exception) {
            Log.w("CyberneticAudioEngine", "AudioTrack synthesis initialization notice: ${e.message}")
        }
    }

    @Synchronized
    fun stop() {
        isEngineRunning = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore clean teardown exceptions
        }
        audioTrack = null
    }

    fun isRunning(): Boolean = isEngineRunning
}
