package com.example.tuneralpha

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tuneralpha.ui.theme.TuneralphaTheme
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.*
import android.os.Build
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.abs
import kotlin.math.abs
import kotlinx.coroutines.isActive
import kotlin.math.abs
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.pitch.PitchDetectionResult
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import be.tarsos.dsp.AudioProcessor
import org.json.JSONObject
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import kotlin.math.roundToInt

var selectedInstrumentName = "Not Selected"
var counter = 0
val MutableList: MutableList<String> = mutableListOf()
var correctPitch = "No Pitch"

// Data model for your JSON structure
data class Instrument(
    val strings: List<StringNote>
)

data class StringNote(
    val string: String,
    val note: String
)

data class InstrumentsResponse(
    val strings: Map<String, Instrument>
)



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TuneralphaTheme {
                var currentScreen by remember { mutableStateOf("home") }
                var selectedInstrument by remember { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "home" -> HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            onModeSelected = { currentScreen = "modeSelection" }, // If you want to keep this option
                            onTunerSelected = { currentScreen = "tuner" } // Navigate to Tuner
                        )
                        "tuner" -> TunerScreen(
                            instrumentName = selectedInstrument,
                            modifier = Modifier.padding(innerPadding),
                            onBack = { currentScreen = "modeSelection" }
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun HomeScreen(modifier: Modifier = Modifier, onModeSelected: () -> Unit, onTunerSelected: () -> Unit) {
    var termsAccepted by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        BackgroundVideo()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color(0xFF000000).copy(alpha = 0.31f)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.your_image),
                contentDescription = null,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.None
            )

            Text(
                text = "NeoTune",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    onTunerSelected() // Navigate to Tuner Screen
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (termsAccepted && privacyAccepted) Color(0xFF6200EE) else Color.Gray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(160.dp, 56.dp),
                enabled = termsAccepted && privacyAccepted
            ) {
                Text(text = "Go to Tuner")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(Color(0xFF6200EE))
                )
                Text(text = "I agree to the Terms of Service", color = Color.White, fontSize = 16.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = privacyAccepted,
                    onCheckedChange = { privacyAccepted = it },
                    colors = CheckboxDefaults.colors(Color(0xFF6200EE))
                )
                Text(text = "I agree to the Privacy Policy", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}






fun checkMicrophonePermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun ShowInstrumentMenu(
    context: Context,
    showMenu: MutableState<Boolean>,
    instrumentName: String
) {
    if (showMenu.value) {
        // Use the correct filename based on where you've placed it in assets
        val instrumentFileName = "json/string_keys_preset.json.txt"
        val jsonObject = loadInstrumentsFromAsset(context, instrumentFileName)

        // Declare instrumentNames here to make it accessible outside of the let block
        val instrumentNames = remember { mutableStateListOf<String>() }

        jsonObject?.let { json ->
            // Retrieve keys of instruments in the "strings" section safely
            json.optJSONObject("strings")?.let { stringsJson ->
                val instruments = stringsJson.keys()
                while (instruments.hasNext()) {
                    val key = instruments.next()
                    instrumentNames.add(key)
                }

                // Log the instrument names

            } ?: Log.e("ShowInstrumentMenu", "No 'strings' found in JSON.")
        } ?: Log.e("ShowInstrumentMenu", "Failed to load JSON.")

        Box(modifier = Modifier.fillMaxSize()) {
            // Dismiss menu when tapping outside of it
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showMenu.value = false } // Tap to dismiss
                    .background(Color.Transparent)
            )

            // Add a column to show instrument names as buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()) // Scrollable column
            ) {
                instrumentNames.forEach { name ->
                    Button(
                        onClick = {
                            val jsonInstrument = jsonObject?.optJSONObject("strings")?.optJSONObject(name) // Get the specific instrument
                            if (jsonInstrument != null) {


                                // Retrieve the strings array
                                val stringsArray = jsonInstrument.optJSONArray("strings")
                                if (stringsArray != null) {
                                    for (i in 0 until stringsArray.length()) {
                                        val stringObject = stringsArray.optJSONObject(i)
                                        stringObject?.let {
                                            val note = it.optString("note") // Retrieve the note
                                            note?.let { MutableList.add(it) } // Add to valArray if not null
                                        }
                                    }

                                    selectedInstrumentName = name
                                    Log.d("Selected Instrument", "Selected: $selectedInstrumentName with notes: $MutableList")
                                    showMenu.value = false // Close the menu after selection
                                } else {
                                    Log.e("Selected Instrument", "No strings found for instrument: $name")
                                }
                            } else {
                                Log.e("Selected Instrument", "Instrument $name not found in JSON.")
                                showMenu.value = false // Ensure menu is closed if instrument is not found
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = name)
                    }
                }
            }
        }
    }
}

@Composable
fun ShowCustomPreset(
    context: Context,
    showMenu: MutableState<Boolean>,
    instrumentName: String
) {

    Box(modifier = Modifier.fillMaxSize()) {
        // Dismiss menu when tapping outside of it
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showMenu.value = false } // Tap to dismiss
                .background(Color.Transparent)
        )

        // Display preset options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0x4D6200EE)) // Example background color
                .verticalScroll(rememberScrollState()) // Make it scrollable
        ) {
            Button(
                onClick = {  },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(160.dp, 56.dp)
            ) {
                Text(text = "Select Custom Preset")
            }
            Button(
                onClick = {  },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(160.dp, 56.dp)
            ) {
                Text(text = "Make Custom Preset")
            }
            Button(
                onClick = {  },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(160.dp, 56.dp)
            ) {
                Text(text = "Delete Custom Preset")
            }
        }
    }
}




private fun loadInstrumentsFromAsset(context: Context, fileName: String): JSONObject? {
    return try {
        // Use a relative path for the JSON file from the assets folder
        val inputStream = context.assets.open(fileName)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        JSONObject(jsonString)
    } catch (e: IOException) {
        Log.e("LoadInstruments", "Error loading JSON", e)
        null
    }
}


fun getPitchForNote(note: String): Float {
    return when (note) {
        // 1st octave
        "A0" -> 27.50f
        "A#0" -> 29.14f
        "B0" -> 30.87f
        "C1" -> 32.70f
        "C#1" -> 34.65f
        "D1" -> 36.71f
        "D#1" -> 38.89f
        "E1" -> 41.20f
        "F1" -> 43.65f
        "F#1" -> 46.25f
        "G1" -> 49.00f
        "G#1" -> 51.91f
        "A1" -> 55.00f
        "A#1" -> 58.27f
        "B1" -> 61.74f

        // 2nd octave
        "C2" -> 65.41f
        "C#2" -> 69.30f
        "D2" -> 73.42f
        "D#2" -> 77.78f
        "E2" -> 82.41f
        "F2" -> 87.31f
        "F#2" -> 92.50f
        "G2" -> 98.00f
        "G#2" -> 103.83f
        "A2" -> 110.00f
        "A#2" -> 116.54f
        "B2" -> 123.47f

        // 3rd octave
        "C3" -> 130.81f
        "C#3" -> 138.59f
        "D3" -> 146.83f
        "D#3" -> 155.56f
        "E3" -> 164.81f
        "F3" -> 174.61f
        "F#3" -> 185.00f
        "G3" -> 196.00f
        "G#3" -> 207.65f
        "A3" -> 220.00f
        "A#3" -> 233.08f
        "B3" -> 246.94f

        // 4th octave
        "C4" -> 261.63f
        "C#4" -> 277.18f
        "D4" -> 293.66f
        "D#4" -> 311.13f
        "E4" -> 329.63f
        "F4" -> 349.23f
        "F#4" -> 369.99f
        "G4" -> 392.00f
        "G#4" -> 415.30f
        "A4" -> 440.00f
        "A#4" -> 466.16f
        "B4" -> 493.88f

        // 5th octave
        "C5" -> 523.25f
        "C#5" -> 554.37f
        "D5" -> 587.33f
        "D#5" -> 622.25f
        "E5" -> 659.26f
        "F5" -> 698.46f
        "F#5" -> 739.99f
        "G5" -> 783.99f
        "G#5" -> 830.61f
        "A5" -> 880.00f
        "A#5" -> 932.33f
        "B5" -> 987.77f

        // 6th octave
        "C6" -> 1046.50f
        "C#6" -> 1108.73f
        "D6" -> 1174.66f
        "D#6" -> 1244.51f
        "E6" -> 1318.51f
        "F6" -> 1396.91f
        "F#6" -> 1479.98f
        "G6" -> 1567.98f
        "G#6" -> 1661.22f
        "A6" -> 1760.00f
        "A#6" -> 1864.66f
        "B6" -> 1975.53f

        // 7th octave
        "C7" -> 2093.00f
        "C#7" -> 2217.46f
        "D7" -> 2349.32f
        "D#7" -> 2489.02f
        "E7" -> 2637.02f
        "F7" -> 2793.83f
        "F#7" -> 2959.96f
        "G7" -> 3135.96f
        "G#7" -> 3322.44f
        "A7" -> 3520.00f
        "A#7" -> 3729.31f
        "B7" -> 3951.07f

        // 8th octave
        "C8" -> 4186.01f

        else -> 0f // Return 0 if the note is not found
    }
}

fun displayCorrectPitch(note: String) {
    val pitch = getPitchForNote(note) // Get the pitch for the given note
    if (pitch > 0) {
        val stringRepresentation: String = pitch.toString()
        correctPitch = stringRepresentation
    } else {
        println("Note $note is not found.")
    }
}


fun getCurrentNote(): String {
    Log.d("counter", "$counter")
    if (MutableList.isNotEmpty() && counter in 0 until MutableList.size) {
        var display1 = MutableList[counter]
        displayCorrectPitch(display1)
        return display1
    } else {
        var display1 = "No Notes"
        return display1
    }
}

// Define this outside of the TunerScreen function
object Processing {
    private val notes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun closestNote(pitchInHz: Float): String {
        val midiNote = (69 + 12 * Math.log(pitchInHz.toDouble() / 440) / Math.log(2.0)).toInt() // Convert pitchInHz to Double
        return notes[(midiNote % 12 + 12) % 12]
    }
}



@Composable
fun TunerScreen(
    instrumentName: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {

    val pitchState = remember { mutableStateOf(0) }
    val currentPitch = remember { mutableStateOf(0f) }
    val showMenu = remember { mutableStateOf(false) }
    val showMenu1 = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var hasMicrophonePermission by remember { mutableStateOf(checkMicrophonePermission(context)) }




    fun startPitchDetection() {
        if (hasMicrophonePermission) {
            Log.d("PitchDetection", "Starting pitch detection...")
            val dispatcher: AudioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 4096, 0)
            val pdh = PitchDetectionHandler { res, _ ->
                val pitchInHz: Float = res.pitch

                if (pitchInHz <= 0) {
                    // Handle the case where the pitch is not valid
                } else {
                    // Convert the pitch from Float to Int
                    val pitchInHzInt: Int = pitchInHz.roundToInt()
                    pitchState.value = pitchInHzInt
                }
            }

            val pitchProcessor: AudioProcessor =
                PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050F, 4096, pdh)
            dispatcher.addAudioProcessor(pitchProcessor)

            val audioThread = Thread(dispatcher, "Audio Thread")
            audioThread.start()
        } else {
            Log.d("PitchDetection", "Microphone permission not granted.")
        }
    }


    // Handle Permission Result
    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasMicrophonePermission = isGranted
        // Restart pitch detection if permission is granted
        if (isGranted) {
            startPitchDetection()
        }
    }

    // Requesting permission if not already granted
    LaunchedEffect(Unit) {
        if (!hasMicrophonePermission) {
            resultLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            // Start pitch detection if permission is granted
            startPitchDetection()
        }
    }





    if (!hasMicrophonePermission) {
        Text("Microphone permission is required for tuning.", color = Color.Red, fontSize = 16.sp)
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            BackgroundVideo()
        }
        if (showMenu.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // Fill the entire screen to manage taps outside

            ) {
                // Dismiss menu when tapping outside of it
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showMenu.value = false } // Click box to dismiss menu
                        .background(Color.Transparent) // Make sure it doesn't take up space
                )

                // Menu Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color(0x4D6200EE)) // Updated to purple with 30% alpha
                        .align(Alignment.Center) // Align the menu at the center
                        .padding(16.dp) // Add padding inside the menu
                ) {
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .background(Color(0xFF000000).copy(alpha = 0.7f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Show instrument selection menu
                        ShowInstrumentMenu(context, showMenu, instrumentName)
                    }
                }
            }
        }

        else if (showMenu1.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // Fill the entire screen to manage taps outside

            ) {
                // Dismiss menu when tapping outside of it
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showMenu1.value = false }
                        .background(Color.Transparent) // Make sure it doesn't take up space
                )

                // Menu Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color(0x4D6200EE)) // Updated to purple with 30% alpha
                        .align(Alignment.Center) // Align the menu at the center
                        .padding(16.dp) // Add padding inside the menu
                ) {
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .background(Color(0xFF000000).copy(alpha = 0.7f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Show instrument selection menu
                        ShowCustomPreset(context, showMenu, instrumentName)
                    }
                }
            }

        }

        else {
            // Only show these buttons if the menu is not open
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(Color(0xFF000000).copy(alpha = 0.31f)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display the instrument name
                Text(
                    text = "Tuning for: $instrumentName",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Display buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 116.dp), // Add bottom margin here
                    horizontalArrangement = Arrangement.SpaceEvenly // Space buttons evenly within the row
                ) {
                    Button(
                        onClick = { showMenu.value = true }, // Show menu when clicked
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(160.dp, 56.dp)
                    ) {
                        Text(text = "Select Instrument Type")
                    }
                    Button(
                        onClick = { showMenu1.value = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(160.dp, 56.dp)
                    ) {
                        Text(text = "Custom Preset")
                    }
                }

                // Additional UI components to show only when the menu is not open
                Row(
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {

                    Text(
                        text = "pitch state: ${pitchState.value}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE),
                        modifier = Modifier.weight(1f) // Optional: Makes the texts share space equally
                    )

                    Text(
                        text = "Correct: ${correctPitch}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE),
                        modifier = Modifier.weight(1f) // Optional: Makes the texts share space equally
                    )
                }

                TunerDial(currentPitchAngle = 30f, targetPitchAngle = 0f)


                Text(

                    text = getCurrentNote(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE),
                    modifier = Modifier.weight(1f) // Optional: Makes the texts share space equally
                )
                // Only display note control buttons when menu is closed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly // Space buttons evenly within the row
                ) {
                    Button(
                        onClick = {
                            if (counter < MutableList.size - 1)
                            {
                                counter++
                                Log.d("size", "${MutableList.size}")
                                Log.d("counter", "$counter")
                                Log.d("array", "Notes in valArray: ${MutableList.joinToString(", ")}")

                            }

                                  },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(160.dp, 56.dp)
                    ) {
                        Text(text = "Next note")
                    }

                    Button(
                        onClick = {
                            if (counter > 0) { // Ensure it does not drop below 0
                                counter--
                                Log.d("counter", "$counter")
                                Log.d("array", "Notes in valArray: ${MutableList.joinToString(", ")}")

                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(160.dp, 56.dp)
                    ) {
                        Text(text = "Last note")
                    }
                }
            }
        }
    }
}




private fun calculatePitchAngle(pitch: Float): Float {
    return when {
        pitch < 100 -> -90f // If pitch is too low
        pitch > 1000 -> 90f // If pitch is too high
        else -> (pitch - 440) / 440 * 90 // Map to -90 to 90 degrees
    }
}


@Composable
fun TunerDial(
    modifier: Modifier = Modifier,
    currentPitchAngle: Float = 0f, // Angle for the current pitch (-90 to 90)
    targetPitchAngle: Float = 0f   // Angle for the correct pitch (-90 to 90)
) {
    Box(
        modifier = modifier.size(400.dp), // Adjust the size
        contentAlignment = Alignment.Center
    ) {
        // Drawing the half-circle dial with lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcThickness = 4.dp.toPx() // Thickness of the arc stroke
            val centerX = size.width / 2
            val centerY = size.height / 2// Center is at the bottom middle of the half-circle

            // Set the radius to ensure the half-circle matches the height of the vertical lines
            val radius = size.width / 2 // Radius to match the width for a perfect half-circle

            // Function to convert angle to radians in the half-circle context (180° to 0°)
            fun angleToRadians(angle: Float): Double {
                // Map -90° to 90° angles to 180° (left) to 0° (right)
                return Math.toRadians(180 - angle.toDouble())
            }

            // Draw half-circle dial background
            drawArc(
                color = Color.Gray.copy(alpha = 0.3f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = arcThickness),
                size = Size(size.width, size.width) // Use width for a perfect half-circle
            )

            // Define the color for the lines
            val lineColor = Color(0xFF6200EE) // Purple color used for the button

            // Draw the target pitch line (always vertical at center)
            drawLine(
                color = lineColor,
                strokeWidth = 4.dp.toPx(),
                start = Offset(centerX, centerY - radius), // Top of the dial
                end = Offset(centerX, centerY) // Bottom of the dial
            )

            // Calculate position for current pitch (rotating line)
            val currentAngleRadians = angleToRadians(currentPitchAngle)
            val currentX = centerX + radius * cos(currentAngleRadians).toFloat()
            val currentY = centerY - radius * sin(currentAngleRadians).toFloat()

            // Draw the current pitch line (rotating line)
            drawLine(
                color = lineColor.copy(alpha = 0.8f), // Slightly more transparent
                strokeWidth = 4.dp.toPx(),
                start = Offset(centerX, centerY), // Bottom of the dial
                end = Offset(currentX, currentY) // Current pitch position
            )
        }
    }
}



@Composable
fun BackgroundVideo() {
    val context = LocalContext.current
    val videoUri = Uri.parse("android.resource://${context.packageName}/raw/background_video") // Use the correct video file name

    AndroidView(
        factory = { context ->
            val frameLayout = android.widget.FrameLayout(context)
            val videoView = object : android.widget.VideoView(context) {
                override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                    val width = MeasureSpec.getSize(widthMeasureSpec)
                    val height = MeasureSpec.getSize(heightMeasureSpec)
                    setMeasuredDimension(width, height)
                }
            }.apply {
                setVideoURI(videoUri)
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true // Loop the video
                    start() // Start playback
                }
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            frameLayout.addView(videoView)
            frameLayout
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TuneralphaTheme {
        HomeScreen(onModeSelected = {}, onTunerSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ModeSelectionScreenPreview() {
    TuneralphaTheme {
        ModeSelectionScreen()
    }
}


