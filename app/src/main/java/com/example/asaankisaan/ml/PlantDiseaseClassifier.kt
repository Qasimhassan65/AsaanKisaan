package com.example.asaankisaan.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PlantDiseaseClassifier(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    
    companion object {
        private const val MODEL_FILE = "plant_disease_model_quant.tflite"
        private const val LABEL_FILE = "disease_mapping_roman_urdu_nextsteps.csv"
        private const val INPUT_SIZE = 224
        private const val NUM_CLASSES = 38 // Model outputs 38 classes
        private const val TAG = "PlantDiseaseClassifier"
        
        // CLASS_NAMES list - EXACTLY matching the CSV file order (lines 2-39)
        // This should match your trained model's class order
        private val CLASS_NAMES = listOf(
            "Apple___Apple_scab",                    // 0 - Line 2 in CSV
            "Apple___Black_rot",                     // 1 - Line 3 in CSV
            "Apple___Cedar_apple_rust",              // 2 - Line 4 in CSV
            "Apple___healthy",                       // 3 - Line 5 in CSV
            "Blueberry___healthy",                   // 4 - Line 6 in CSV
            "Cherry_(including_sour)___Powdery_mildew", // 5 - Line 7 in CSV
            "Cherry_(including_sour)___healthy",     // 6 - Line 8 in CSV
            "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot", // 7 - Line 9 in CSV
            "Corn_(maize)___Common_rust_",           // 8 - Line 10 in CSV
            "Corn_(maize)___Northern_Leaf_Blight",   // 9 - Line 11 in CSV
            "Corn_(maize)___healthy",                // 10 - Line 12 in CSV
            "Grape___Black_rot",                     // 11 - Line 13 in CSV
            "Grape___Esca_(Black_Measles)",          // 12 - Line 14 in CSV
            "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)", // 13 - Line 15 in CSV
            "Grape___healthy",                       // 14 - Line 16 in CSV
            "Orange___Haunglongbing_(Citrus_greening)", // 15 - Line 17 in CSV
            "Peach___Bacterial_spot",                // 16 - Line 18 in CSV
            "Peach___healthy",                       // 17 - Line 19 in CSV
            "Pepper,_bell___Bacterial_spot",         // 18 - Line 20 in CSV
            "Pepper,_bell___healthy",                // 19 - Line 21 in CSV
            "Potato___Early_blight",                 // 20 - Line 22 in CSV
            "Potato___Late_blight",                  // 21 - Line 23 in CSV
            "Potato___healthy",                      // 22 - Line 24 in CSV
            "Raspberry___healthy",                   // 23 - Line 25 in CSV
            "Soybean___healthy",                     // 24 - Line 26 in CSV
            "Squash___Powdery_mildew",               // 25 - Line 27 in CSV
            "Strawberry___Leaf_scorch",              // 26 - Line 28 in CSV
            "Strawberry___healthy",                  // 27 - Line 29 in CSV
            "Tomato___Bacterial_spot",               // 28 - Line 30 in CSV
            "Tomato___Early_blight",                 // 29 - Line 31 in CSV
            "Tomato___Late_blight",                  // 30 - Line 32 in CSV
            "Tomato___Leaf_Mold",                    // 31 - Line 33 in CSV
            "Tomato___Septoria_leaf_spot",           // 32 - Line 34 in CSV
            "Tomato___Spider_mites Two-spotted_spider_mite", // 33 - Line 35 in CSV
            "Tomato___Target_Spot",                  // 34 - Line 36 in CSV
            "Tomato___Tomato_Yellow_Leaf_Curl_Virus", // 35 - Line 37 in CSV
            "Tomato___Tomato_mosaic_virus",          // 36 - Line 38 in CSV
            "Tomato___healthy"                       // 37 - Line 39 in CSV
        )
    }
    
    // Disease mapping from CSV
    private val diseaseMapping = mutableMapOf<String, DiseaseInfo>()
    
    data class DiseaseInfo(
        val englishName: String,
        val romanUrduHindiCommon: String,
        val nextSteps: String
    )
    
    data class PredictionResult(
        val diseaseName: String,
        val confidence: Float,
        val diseaseInfo: DiseaseInfo?
    )
    
    init {
        loadModel()
        loadDiseaseMapping()
    }
    
    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options()
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model: ${e.message}")
            throw e
        }
    }
    
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    private fun loadDiseaseMapping() {
        try {
            val inputStream = context.assets.open(LABEL_FILE)
            val reader = inputStream.bufferedReader()
            val lines = reader.readLines()
            
            // Skip header line
            for (i in 1 until lines.size) {
                val parts = lines[i].split(",")
                if (parts.size >= 3) {
                    val englishName = parts[0].trim()
                    val romanUrdu = parts[1].trim()
                    val nextSteps = parts[2].trim()
                    
                    diseaseMapping[englishName] = DiseaseInfo(englishName, romanUrdu, nextSteps)
                }
            }
            
            Log.d(TAG, "Loaded ${diseaseMapping.size} disease mappings")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading disease mapping: ${e.message}")
        }
    }
    
    private fun createImageProcessor(): ImageProcessor {
        return ImageProcessor.Builder()
            .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Normalize to [0, 1] range (divide by 255.0)
            .build()
    }
    
    fun classifyDisease(bitmap: Bitmap): PredictionResult {
        try {
            val interpreter = this.interpreter ?: throw IllegalStateException("Model not loaded")
            
            Log.d(TAG, "Starting classification with bitmap size: ${bitmap.width}x${bitmap.height}")
            Log.d(TAG, "Bitmap config: ${bitmap.config}")
            Log.d(TAG, "Available classes count: ${CLASS_NAMES.size}")
            
            // Convert bitmap to ARGB_8888 format
            val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            Log.d(TAG, "Converted bitmap config: ${argbBitmap.config}")
            
            // Prepare input image with proper format
            val tensorImage = TensorImage()
            tensorImage.load(argbBitmap)
            
            // Preprocess image: Resize to 224x224 and normalize to [0, 1]
            val imageProcessor = createImageProcessor()
            val processedImage = imageProcessor.process(tensorImage)
            
            // Get model info for verification
            val inputShape = interpreter.getInputTensor(0).shape()
            val outputShape = interpreter.getOutputTensor(0).shape()
            Log.d(TAG, "Model input shape: ${inputShape.contentToString()}")
            Log.d(TAG, "Model output shape: ${outputShape.contentToString()}")
            
            // Prepare output array for [1, 38] probabilities
            val output = Array(1) { FloatArray(NUM_CLASSES) }
            
            Log.d(TAG, "Running inference...")
            
            // Run inference
            interpreter.run(processedImage.buffer, output)
            
            // Get prediction results - output[0] contains 38 probabilities
            val probabilities = output[0]
            
            // Find argmax - index of highest probability
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val confidence = probabilities[maxIndex]
            
            // Map index to class name using CLASS_NAMES
            val predictedClassName = if (maxIndex < CLASS_NAMES.size) {
                CLASS_NAMES[maxIndex]
            } else {
                Log.w(TAG, "Model output index $maxIndex exceeds CLASS_NAMES size ${CLASS_NAMES.size}")
                CLASS_NAMES[0] // Fallback to first class
            }
            
            // Get disease info from mapping (will be updated after finalDiseaseName is determined)
            
            // Debug: Log top 5 predictions
            val sortedPredictions = probabilities.mapIndexed { index, prob -> 
                index to prob 
            }.sortedByDescending { it.second }.take(5)
            
            Log.d(TAG, "Top 5 predictions:")
            sortedPredictions.forEach { (index, prob) ->
                val className = if (index < CLASS_NAMES.size) CLASS_NAMES[index] else "Unknown"
                Log.d(TAG, "  $index: $className = $prob")
            }
            
            // Check if confidence is too low - might indicate wrong classification
            if (confidence < 0.5f) {
                Log.w(TAG, "⚠️ Low confidence prediction: $confidence - model might be uncertain")
            }
            
            Log.d(TAG, "Final prediction: $predictedClassName with confidence: $confidence")
            
            // Debug: Show what the model is actually predicting
            Log.d(TAG, "=== MODEL ANALYSIS ===")
            Log.d(TAG, "Model predicts index $maxIndex with confidence $confidence")
            Log.d(TAG, "Top 5 predictions:")
            sortedPredictions.take(5).forEach { (index, prob) ->
                val className = if (index < CLASS_NAMES.size) CLASS_NAMES[index] else "Unknown"
                Log.d(TAG, "  Index $index: $className = $prob")
            }
            
            // Check if model is working correctly by looking at probability distribution
            val nonZeroPredictions = probabilities.count { it > 0.01f }
            Log.d(TAG, "Non-zero predictions (>0.01): $nonZeroPredictions out of 38")
            
            if (nonZeroPredictions < 5) {
                Log.w(TAG, "⚠️ Model seems to be predicting very few classes - possible issue with model or input")
            }
            
            // Use the model's actual prediction with new class mapping
            val finalDiseaseName = if (confidence < 0.3f) {
                Log.w(TAG, "Very low confidence ($confidence) - providing fallback info")
                "Uncertain Classification - $predictedClassName"
            } else {
                predictedClassName
            }
            
            // Get disease info from mapping for the final disease name
            val finalDiseaseInfo = diseaseMapping[finalDiseaseName]
            
            return PredictionResult(
                diseaseName = finalDiseaseName,
                confidence = confidence,
                diseaseInfo = finalDiseaseInfo
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during classification: ${e.message}", e)
            return PredictionResult(
                diseaseName = "Unknown",
                confidence = 0f,
                diseaseInfo = null
            )
        }
    }
    
    
    fun getAvailableDiseases(): List<DiseaseInfo> {
        return diseaseMapping.values.toList()
    }
    
    // Test method to verify model is working
    fun testModel(): Boolean {
        return try {
            val interpreter = this.interpreter
            if (interpreter == null) {
                Log.e(TAG, "Model not loaded")
                false
            } else {
                val inputShape = interpreter.getInputTensor(0).shape()
                val outputShape = interpreter.getOutputTensor(0).shape()
                Log.d(TAG, "Model test successful - Input: ${inputShape.contentToString()}, Output: ${outputShape.contentToString()}")
                Log.d(TAG, "CLASS_NAMES loaded: ${CLASS_NAMES.size}")
                Log.d(TAG, "Disease mappings loaded: ${diseaseMapping.size}")
                
                // Verify output shape matches expected
                if (outputShape[1] == NUM_CLASSES) {
                    Log.d(TAG, "✅ Model output shape matches expected: [1, $NUM_CLASSES]")
                } else {
                    Log.w(TAG, "⚠️ Model output shape mismatch: expected [1, $NUM_CLASSES], got ${outputShape.contentToString()}")
                }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Model test failed: ${e.message}", e)
            false
        }
    }
    
    fun cleanup() {
        interpreter?.close()
        interpreter = null
    }
}
