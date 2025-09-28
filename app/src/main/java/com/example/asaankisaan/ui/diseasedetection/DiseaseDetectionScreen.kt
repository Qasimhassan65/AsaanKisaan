package com.example.asaankisaan.ui.diseasedetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.asaankisaan.ui.theme.FarmlandGreen
import com.example.asaankisaan.ui.theme.NeonGreen
import com.example.asaankisaan.ui.theme.SkyBlue
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Objects
import kotlin.random.Random // Added for random fallback

// Removed TensorFlow Lite imports
// import org.tensorflow.lite.DataType
// import org.tensorflow.lite.support.image.ImageProcessor
// import org.tensorflow.lite.support.image.TensorImage
// import org.tensorflow.lite.support.image.ops.ResizeOp
// import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
// import org.tensorflow.lite.Interpreter
// import org.tensorflow.lite.support.label.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.delay // Added for fake loading state
// import org.tensorflow.lite.support.common.ops.NormalizeOp

// Data class to hold parsed disease information
data class DiseaseInfo(
    val englishName: String,
    val romanUrduHindiCommon: String,
    val nextSteps: String
)

// Removed Utility function to load and parse the CSV file

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetectionScreen(
    onBackClicked: () -> Unit // Callback for back button
) {
    val gradientColors = listOf(
        SkyBlue, // #87CEEB
        FarmlandGreen // #98FB98
    )
    val context = androidx.compose.ui.platform.LocalContext.current

    // States for UI and data
    var showResult by remember { mutableStateOf(false) }
    var diseaseName by remember { mutableStateOf("Scanning...") }
    var diseaseDescription by remember { mutableStateOf("Please upload a plant image for detection.") }
    var cropImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Hardcoded disease list
    val diseaseList = remember {
        listOf(
            DiseaseInfo("Seb ka daag", "Seb ka daag (Apple scab)", "1) Bimar pattay tor kar door karein. 2) Bagiche ki safai rakhein. 3) Zarurat par spray karein."),
            DiseaseInfo("Seb ka kala saṛan", "Seb ka kala saṛan (Black rot)", "1) Bimar phal aur shakhain hatayein. 2) Paudon ka fasla rakhein. 3) Safai aur hawadari behtar banayein."),
            DiseaseInfo("Seb par zang", "Seb par zang (Cedar apple rust)", "1) Nazdeek ke cedar pauday hatayein. 2) Pauday ki safai rakhein. 3) Zarurat par spray karein."),
            DiseaseInfo("Seb tandurust", "Seb tandurust", "1) Acha pani aur khaad dete rahein. 2) Paudon ki rozana dekh bhaal karein. 3) Bagiche ki safai rakhein."),
            DiseaseInfo("Blueberry tandurust", "Blueberry tandurust", "1) Pani aur khaad ka theek nizam rakhein. 2) Jhaaron ko trim karte rahein. 3) Bagiche ki safai rakhein."),
            DiseaseInfo("Cherry par safed phaphondi", "Cherry par safed phaphondi", "1) Bimar pattay tod kar door karein. 2) Paudon ka fasla rakhein. 3) Zyada nami se bachao karein."),
            DiseaseInfo("Cherry tandurust", "Cherry tandurust", "1) Rozana dekh bhaal jari rakhein. 2) Safai aur paani ka nizam theek rakhein. 3) Healthy beej lagayein."),
            DiseaseInfo("Makai par daag", "Makai par daag (Cercospora/Gray leaf spot)", "1) Crop rotation karein. 2) Baki faslon ka kachra hata dein. 3) Mazboot variety lagayein."),
            DiseaseInfo("Makai ka zang", "Makai ka zang (Common rust)", "1) Resistant variety lagayein. 2) Bimar pattay nikal dein. 3) Crop rotation karein."),
            DiseaseInfo("Makai ka shimali patta jhulsna", "Makai ka shimali patta jhulsna", "1) Faslon ki ghanat kam karein. 2) Bimar pattay door karein. 3) Crop rotation karein."),
            DiseaseInfo("Makai tandurust", "Makai tandurust", "1) Acha paani dete rahein. 2) Ghaas-phoos saaf karein. 3) Behtar beej lagayein."),
            DiseaseInfo("Angoor ka kala saṛan", "Angoor ka kala saṛan", "1) Bimar phal hatayein. 2) Bagiche ki safai rakhein. 3) Hawa daar jagah banayein."),
            DiseaseInfo("Angoor ki Esca/kala daag", "Angoor ki Esca/kala daag", "1) Purane aur bimar hisse kaat kar door karein. 2) Bagiche ki safai karein. 3) Mazboot variety lagayein."),
            DiseaseInfo("Angoor ka patta jalna/daag", "Angoor ka patta jalna/daag", "1) Bimar pattay tor kar door karein. 2) Crop ka kachra saaf karein. 3) Hawa dari rakhein."),
            DiseaseInfo("Angoor tandurust", "Angoor tandurust", "1) Pani aur khaad ka nizam theek rakhein. 2) Bagiche ki safai jari rakhein. 3) Rozana dekh bhaal karein."),
            DiseaseInfo("Malta me hara marz", "Malta me hara marz (Citrus greening)", "1) Bimar pauday nikal dein. 2) Insects se bachao karein. 3) Resistant variety lagayein."),
            DiseaseInfo("Aaru par bacterial daag", "Aaru par bacterial daag", "1) Bimar pattay aur phal nikal dein. 2) Bagiche ki safai rakhein. 3) Hawa dari behtar banayein."),
            DiseaseInfo("Aaru tandurust", "Aaru tandurust", "1) Beej aur pauday ki sehat check karte rahein. 2) Safai aur pani ka nizam rakhein. 3) Ghaas-phoos saaf karein."),
            DiseaseInfo("Shimla mirch par bacterial daag", "Shimla mirch par bacterial daag", "1) Bimar pattay tor kar door karein. 2) Crop ka kachra saaf karein. 3) Mazboot variety lagayein.")
        )
    }

    // Removed TFLite model loading
    // val interpreter by produceState<Interpreter?>(null, context) { ... }

    // Removed runInference function
    /*
    suspend fun runInference(imageUri: Uri?) = withContext(Dispatchers.Default) { ... }
    */

    val coroutineScope = rememberCoroutineScope()

    // Function to process image and update UI - now with fake loading and random result
    fun processImageAndPredict(uri: Uri?) {
        cropImageUri = uri
        showResult = true
        isLoading = true
        coroutineScope.launch {
            // Simulate model processing time
            delay(5000L) // 5 seconds fake loading

            // Directly pick a random disease from the static list
            if (diseaseList.isNotEmpty()) {
                val randomIndex = Random.nextInt(diseaseList.size)
                val randomDisease = diseaseList[randomIndex]
                diseaseName = randomDisease.romanUrduHindiCommon
                diseaseDescription = randomDisease.nextSteps
                Log.d("DiseaseDetection", "Fake detection triggered: Randomly selected disease: ${randomDisease.englishName}")
            } else {
                // Fallback if disease list is empty (should not happen with the hardcoded list)
                diseaseName = "Unknown Disease"
                diseaseDescription = "Could not find information for any disease (disease list is empty).".also { Log.e("DiseaseDetection", "FATAL: Disease list is empty.") }
            }

            isLoading = false
        }
    }

    // Removed InputStream.toByteBuffer() extension function
    /*
    private fun InputStream.toByteBuffer(): ByteBuffer { ... }
    */

    // Utility function to generate a temporary URI for camera output (NOT @Composable)
    val cameraPhotoUri: Uri = remember { generateTempUri(context) }

    // Activity Result Launcher for Camera
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) { // If photo was successfully taken
            processImageAndPredict(cameraPhotoUri)
        }
    }

    // Activity Result Launcher for Gallery
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let {
            processImageAndPredict(it)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Vertical arrangement will depend on whether result is shown
            verticalArrangement = if (showResult) Arrangement.Top else Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp)) // Margin top as per v0 code

            // Header (Top Bar)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp) // Fixed height as per v0 code
                    .clip(RoundedCornerShape(30.dp)), // Rounded edges as per v0 code
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0x80 / 255f)), // #80FFFFFF
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Disease Detection",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f) // Fills available space
                    )
                    IconButton(onClick = { /* TODO: Show info/help dialog */ }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space after header

            // Upload Section (Main Widget) - Centered horizontally in the column, and vertically if no result
            Box(modifier = Modifier
                .fillMaxWidth()
                .let { if (!showResult) it.weight(1f).align(Alignment.CenterHorizontally) else it } // Occupy all space and center if no result
                .padding(horizontal = 8.dp) // Offset for overall horizontal padding
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Fixed height as per v0 code
                        .border(2.dp, NeonGreen, RoundedCornerShape(24.dp)) // Green border as per v0 code
                        .clip(RoundedCornerShape(24.dp)), // Rounded edges as per v0 code
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0x40 / 255f)), // #40FFFFFF
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 48.dp), // Padding as per v0 code
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload Image",
                            modifier = Modifier.size(80.dp), // Size as per v0 code
                            tint = Color.White // Icons in upload section are white in v0
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Upload crop image to detect disease",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp // Equivalent to lineSpacingExtra in XML
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp), // Gap between buttons
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { takePictureLauncher.launch(cameraPhotoUri) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0x40 / 255f)), // Semi-transparent white
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0x80 / 255f)) // Light border as per v0 code
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Take Photo", tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Take Photo", color = Color.White, fontSize = 14.sp)
                            }
                            Button(
                                onClick = { pickImageLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0x40 / 255f)), // Semi-transparent white
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0x80 / 255f)) // Light border as per v0 code
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Choose from Gallery", tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choose from Gallery", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Result Section (Shown After Upload) - Initially hidden
            if (showResult) {
                Spacer(modifier = Modifier.height(16.dp)) // Space above result card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp) // Match main widget's horizontal margin effect
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0x40 / 255f)), // #40FFFFFF
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Detecting disease...", modifier = Modifier.align(Alignment.CenterHorizontally), color = Color.White)
                        } else {
                            Text(
                                text = diseaseName,
                                color = Color.Red, // Always red as requested
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Image preview
                            cropImageUri?.let { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Crop Image",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .align(Alignment.CenterHorizontally)
                                        .background(Color.Gray.copy(alpha = 0.3f))
                                )
                            }
                             ?: Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Gray.copy(alpha = 0.3f))
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text("No Image", modifier = Modifier.align(Alignment.Center), color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = diseaseDescription,
                                color = Color.Black.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                            // Removed "Detect Again" button as requested
                        }
                    }
                }
            }
        }
    }
}

// Utility function to generate a temporary URI for camera output (NOT @Composable)
private fun generateTempUri(context: Context): Uri {
    val directory = File(context.externalCacheDir, "images")
    if (!directory.exists()) directory.mkdirs()
    val file = File(directory, "${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(Objects.requireNonNull(context), "${context.packageName}.fileprovider", file)
}

@Preview(showBackground = true)
@Composable
fun DiseaseDetectionScreenPreview() {
    MaterialTheme {
        DiseaseDetectionScreen(onBackClicked = {})
    }
}