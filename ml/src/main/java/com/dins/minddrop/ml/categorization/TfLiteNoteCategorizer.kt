package com.dins.minddrop.ml.categorization

import android.content.Context
import android.util.Log
import com.dins.minddrop.domain.model.NoteType
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

// No trained model is bundled for this project -- training a real classifier
// for these 5 note types needs a labeled dataset and a training pipeline
// outside this app's scope. This class still owns the real integration
// point (asset loading, tensor I/O), so categorize() returns null cleanly
// whenever the model is missing/fails, and CompositeNoteCategorizer falls
// back to KeywordNoteCategorizer -- the graceful-degradation path Day 11
// asks for, exercised for real rather than simulated.
@Singleton
class TfLiteNoteCategorizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val interpreter: Interpreter? = runCatching { loadInterpreter() }
        .onFailure { e -> Log.w(TAG, "TFLite model unavailable, using keyword fallback: ${e.message}") }
        .getOrNull()

    fun categorize(content: String): NoteType? {
        val model = interpreter ?: return null
        return runCatching {
            val input = encode(content)
            val output = Array(1) { FloatArray(NoteType.entries.size) }
            model.run(input, output)
            val bestIndex = output[0].indices.maxByOrNull { output[0][it] } ?: return null
            NoteType.entries[bestIndex]
        }.onFailure { e -> Log.w(TAG, "TFLite inference failed, using keyword fallback: ${e.message}") }
            .getOrNull()
    }

    private fun loadInterpreter(): Interpreter {
        val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
        val modelBuffer: MappedByteBuffer = FileInputStream(assetFileDescriptor.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
        return Interpreter(modelBuffer)
    }

    // Placeholder bag-of-words encoding; a real deployment would use the
    // same tokenizer/vocabulary the bundled model was trained with.
    private fun encode(content: String): Array<FloatArray> {
        val vector = FloatArray(VOCAB_SIZE)
        content.lowercase().split(Regex("\\W+")).forEach { word ->
            if (word.isEmpty()) return@forEach
            val index = word.hashCode().mod(VOCAB_SIZE)
            vector[index] += 1f
        }
        return arrayOf(vector)
    }

    private companion object {
        const val TAG = "TfLiteNoteCategorizer"
        const val MODEL_FILE = "note_categorizer.tflite"
        const val VOCAB_SIZE = 128
    }
}
