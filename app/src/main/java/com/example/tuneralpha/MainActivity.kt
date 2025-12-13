
package com.example.tuneralpha
import be.tarsos.dsp.filters.BandPass
import android.Manifest
import androidx.compose.ui.res.painterResource
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import com.example.tuneralpha.ui.theme.TuneralphaTheme
import java.util.Locale
import kotlin.math.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.annotation.DrawableRes



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TuneralphaTheme {
                // Set the initial screen to "tuner" instead of "home"
                val currentScreen by remember { mutableStateOf("tuner") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "tuner" -> TunerScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

fun checkMicrophonePermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
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

fun getNoteForPitch(pitch: Float): String {
    // List of target pitches in ascending order
    val pitchValues =
        listOf(
            27f,
            29f,
            31f,
            33f,
            35f,
            37f,
            39f,
            41f,
            44f,
            46f,
            49f,
            52f,
            55f,
            58f,
            62f,
            65f,
            69f,
            73f,
            78f,
            82f,
            87f,
            93f,
            98f,
            104f,
            110f,
            117f,
            123f,
            131f,
            139f,
            147f,
            156f,
            165f,
            175f,
            185f,
            196f,
            208f,
            220f,
            233f,
            247f,
            262f,
            277f,
            294f,
            311f,
            330f,
            349f,
            370f,
            392f,
            415f,
            440f,
            466f,
            494f,
            523f,
            554f,
            587f,
            622f,
            659f,
            698f,
            740f,
            784f,
            831f,
            880f,
            932f,
            988f,
            1047f,
            1109f,
            1175f,
            1245f,
            1319f,
            1397f,
            1480f,
            1568f,
            1661f,
            1760f,
            1865f,
            1976f,
            2093f,
            2217f,
            2349f,
            2489f,
            2637f,
            2794f,
            2960f,
            3136f,
            3322f,
            3520f,
            3729f,
            3951f,
            4186f
        )

    // Corresponding notes aligned with pitchValues list
    val notes =
        listOf(
            "A0 ",
            "A#0",
            "B0 ",
            "C1 ",
            "C#1",
            "D1 ",
            "D#1",
            "E1 ",
            "F1 ",
            "F#1",
            "G1 ",
            "G#1",
            "A1 ",
            "A#1",
            "B1 ",
            "C2 ",
            "C#2",
            "D2 ",
            "D#2",
            "E2 ",
            "F2 ",
            "F#2",
            "G2 ",
            "G#2",
            "A2 ",
            "A#2",
            "B2 ",
            "C3 ",
            "C#3",
            "D3 ",
            "D#3",
            "E3 ",
            "F3 ",
            "F#3",
            "G3 ",
            "G#3",
            "A3 ",
            "A#3",
            "B3 ",
            "C4 ",
            "C#4",
            "D4 ",
            "D#4",
            "E4 ",
            "F4 ",
            "F#4",
            "G4 ",
            "G#4",
            "A4 ",
            "A#4",
            "B4 ",
            "C5 ",
            "C#5",
            "D5 ",
            "D#5",
            "E5 ",
            "F5 ",
            "F#5",
            "G5 ",
            "G#5",
            "A5 ",
            "A#5",
            "B5 ",
            "C6 ",
            "C#6",
            "D6 ",
            "D#6",
            "E6 ",
            "F6 ",
            "F#6",
            "G6 ",
            "G#6",
            "A6 ",
            "A#6",
            "B6 ",
            "C7 ",
            "C#7",
            "D7 ",
            "D#7",
            "E7 ",
            "F7 ",
            "F#7",
            "G7 ",
            "G#7",
            "A7 ",
            "A#7",
            "B7 ",
            "C8 "
        )

    // Find the index of the closest pitch value
    val index =
        pitchValues.indices.minByOrNull { abs(pitch - pitchValues[it]) } ?: return "Unknown Note"

    // Return the corresponding note
    return notes[index]
}




@Composable
fun TunerScreen(modifier: Modifier = Modifier) {

    val pitchState = remember { mutableFloatStateOf(0f) }
    val notesList = listOf("E4", "B3", "G3", "D3", "A2", "E2")
    val mutableList = remember { mutableStateOf(notesList.toMutableList()) }
    var counter by remember { mutableIntStateOf(0) }
    val currentNote = remember { mutableStateOf("") }
    val arrowState = remember { mutableStateOf("") }
    val context = LocalContext.current
    var hasMicrophonePermission by remember { mutableStateOf(checkMicrophonePermission(context)) }
    var correctPitch by remember { mutableStateOf("No Pitch") }
    var correctNotePitch by remember { mutableFloatStateOf(0f) }
    val noteSet = remember { mutableStateOf(false) }

    fun displayCorrectPitch(note: String) {
        val pitch = getPitchForNote(note)
        println(" the pitch of the correct note $pitch")
        val correctPitchNote = note // Get the pitch for the given note
        if (pitch > 0) {

            correctPitch = correctPitchNote
            correctNotePitch = pitch
            noteSet.value = true
        } else {
            println("Note $note is not found.")
        }
    }

    // Initialize the first note on load
    LaunchedEffect(Unit) {
        if (mutableList.value.isNotEmpty()) {
            currentNote.value = mutableList.value[0]
            displayCorrectPitch(mutableList.value[0])
        }
    }

    val isFrozen = remember { mutableStateOf(false) }

    // Observe arrowState and trigger freeze on "check"
    LaunchedEffect(arrowState.value) {
        if (arrowState.value == "check") {
            isFrozen.value = true
            // Launch a coroutine to unfreeze after 2 seconds
            launch {
                delay(2000L)
                isFrozen.value = false
            }
        }
    }

    @DrawableRes
    fun getImageForNote(note: String): Int {
        return when (note) {
            "E4" -> R.drawable.one
            "B3" -> R.drawable.two
            "G3" -> R.drawable.three
            "D3" -> R.drawable.four
            "A2" -> R.drawable.five
            "E2" -> R.drawable.six
            else -> R.drawable.your_image1
        }
    }

    fun startPitchDetection() {
        if (hasMicrophonePermission) {
            Log.d("PitchDetection", "Starting pitch detection...")
            if (!noteSet.value) {
                println("No note selected. Pitch detection will not start.")
                return
            }


            // List to hold the last 5 pitch values
            val lastPitches = mutableListOf<Float>()
            var lastNote: String? = null
            var lastResetTime = System.currentTimeMillis()
            val bandPassFilter = BandPass(40.0f, 1000.0f, 22050.0f)
            val dispatcher: AudioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 8192, 0)
            dispatcher.addAudioProcessor(bandPassFilter)



            var lastProcessTime = System.currentTimeMillis() - 1000
            val pdh = PitchDetectionHandler { res, _ ->
                val pitchInHz: Float = res.pitch
                val currentTime = System.currentTimeMillis()

                // Enforce 1-second delay between processing
                if (currentTime - lastProcessTime >= 1000) {
                    lastProcessTime = currentTime

                if (pitchInHz > 0) {
                    // Only process if within guitar frequency range
                    if (pitchInHz in 40f..1000f) {
                        // Existing code to process pitch
                        // Check if note has changed, update state, etc.
                        val currentNoteStr = getNoteForPitch(pitchInHz)
                        if (currentNoteStr != lastNote) {
                            lastPitches.clear()
                            lastNote = currentNoteStr
                            lastResetTime = System.currentTimeMillis()
                        }

                        // Reset list periodically
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastResetTime > 1000) {
                            lastPitches.clear()
                            lastResetTime = currentTime
                        }

                        // Only proceed if within guitar range
                        if (abs(pitchInHz - correctNotePitch) <= 200f) {
                            lastPitches.add(pitchInHz)
                            // Keep only last 20 values
                            if (lastPitches.size > 1) {
                                lastPitches.removeAt(0)
                            }

                            // Compute the average of the last 20 pitches
                            val averagePitch = lastPitches.sum() / lastPitches.size

                            //val latestPitch = lastPitches.last()
                            val roundedAverage = String.format(Locale.US, "%.2f", averagePitch).toFloat()
                            currentNote.value = getNoteForPitch(roundedAverage)
                            pitchState.floatValue = roundedAverage
                        }}
                    } else {
                        // Ignore pitches outside guitar range
                    }
                }



                else {
                    // Handle invalid pitch if necessary
                }
            }

            val pitchProcessor: AudioProcessor =
                PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050F, 8192, pdh)
            dispatcher.addAudioProcessor(pitchProcessor)

            val audioThread = Thread(dispatcher, "Audio Thread")
            audioThread.start()
        } else {
            Log.d("PitchDetection", "Microphone permission not granted.")
        }
    }

    // Handle Permission Result
    val resultLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
                isGranted: Boolean ->
            hasMicrophonePermission = isGranted
            // Restart pitch detection if permission is granted
            if (isGranted) {
                startPitchDetection()
            }
        }
    fun getPitchDirection(
        currentPitch: Float,
        correctPitch: Float,
        tolerance: Float = 1f
    ): String {
        return when {
            abs(currentPitch - correctPitch) <= tolerance ->
                "check" // Within tolerance: indicate correct
            currentPitch > correctPitch -> "up" // Current pitch is higher
            else -> "down" // Current pitch is lower
        }
    }

    fun getCurrentNote(): String {
        Log.d("counter", "$counter.value}")
        if (mutableList.value.isNotEmpty() && counter in 0 until mutableList.value.size) {
            val display1 = mutableList.value[counter]
            displayCorrectPitch(display1)
            return display1
        } else {
            val display1 = "No Notes"
            return display1
        }
    }





    fun noteToGuitarString(note: String): String {
        return when (note.trim()) {

            "E4" -> "1st string (E)"
            "B3" -> "2nd string (B)"
            "G3" -> "3rd string (G)"
            "D3" -> "4th string (D)"
            "A2" -> "5th string (A)"
            "E2" -> "6th string (E)"

            else -> "Unknown     "
        }
    }

    fun currentNote(): String {
        val note = currentNote.value
        return noteToGuitarString(note)
    }

    fun correctString(): String {
        val noteP = getNoteForPitch(correctNotePitch)
        return noteToGuitarString(noteP)
    }

    fun correctNote(): String {
        val noteP = getNoteForPitch(correctNotePitch)
        return noteP
    }

    LaunchedEffect(pitchState.floatValue, correctNotePitch) {
        arrowState.value = getPitchDirection(pitchState.floatValue, correctNotePitch)
    }

    LaunchedEffect(counter) { currentNote.value = getCurrentNote() }

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
        Box(modifier = modifier.fillMaxSize()) { BackgroundVideo() }
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF000000).copy(alpha = 0.31f)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier =
                Modifier.size(width = 200.dp, height = 40.dp) // Set the desired size
                    .padding(bottom = 32.dp) // Adjust the padding as needed
            ) {
                // This box is empty but takes up space
            }
            val rawNote = correctNote()
            val trimmedNote = rawNote.trim()
            val imageResId = getImageForNote(trimmedNote)
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Note Image",
                modifier = Modifier
                    .size(300.dp)
                    .padding(bottom = 64.dp)
            )


            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Move: ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF808080),
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.size(70.dp)) {
                    when (arrowState.value) {
                        "check" ->
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Correct",
                                tint = Color.Green,
                                modifier = Modifier.fillMaxSize()
                            )
                        "up" ->
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Too High",
                                tint = Color.Red,
                                modifier = Modifier.fillMaxSize()
                            )
                        "down" ->
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Too Low",
                                tint = Color.Red,
                                modifier = Modifier.fillMaxSize()
                            )
                        else -> {} // No icon
                    }
                }
                Text(
                    text = "To Match Values",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF808080),
                    // No weight here, so this will take only necessary space
                )
            }




            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Correct Pitch: ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF808080),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = " $correctNotePitch ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF),
                    // No weight here, so this will take only necessary space
                )
                Text(
                    text = " ${correctString()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF808080),
                    // No weight here, so this will take only necessary space
                )
            }




            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current pitch:",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF808080),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = " ${String.format("%.2f", pitchState.floatValue)} ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF),
                    // No weight here, so this will take only necessary space
                )
                Text(
                    text = " ${currentNote()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF808080),
                    // No weight here, so this will take only necessary space
                )
            }

            //
            //                TunerDial(currentPitchAngle = currentPitchAngle.value,
            // targetPitchAngle = 0f, pitchState = pitchState.value)

            // Only display note control buttons when menu is closed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                Arrangement.SpaceEvenly // Space buttons evenly within the row
            ) {
                Button(
                    onClick = {
                        if (counter < mutableList.value.size - 1)
                            if (counter < mutableList.value.size - 1) {
                                counter++
                                currentNote.value = mutableList.value[counter]
                                displayCorrectPitch(mutableList.value[counter])
                            }
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.size(160.dp, 56.dp)
                ) {
                    Text(text = "Last String")
                }

                Button(
                    onClick = {
                        if (counter > 0) {
                            counter--
                            currentNote.value = mutableList.value[counter]
                            displayCorrectPitch(mutableList.value[counter])
                        }
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE).copy(alpha = 0.4f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.size(160.dp, 56.dp)
                ) {
                    Text(text = "Next String")
                }

            }

            Box(
                modifier =
                Modifier.size(width = 200.dp, height = 40.dp) // Set the desired size
                    .padding(bottom = 32.dp) // Adjust the padding as needed
            ) {
                // This box is empty but takes up space

            }
            Text(
                text = "© 2025 Oboukhov Entertainment LLC. All rights reserved.",
                fontSize = 15.sp,

                color = Color(0xFF808080),

            )
        }
    }
}

@Composable
fun BackgroundVideo() {
    val context = LocalContext.current
    val videoUri =
        Uri.parse(
            "android.resource://${context.packageName}/raw/background_video"
        ) // Use the correct video file name

    AndroidView(
        factory = { context ->
            val frameLayout = android.widget.FrameLayout(context)
            val videoView =
                object : android.widget.VideoView(context) {
                    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                        val width = MeasureSpec.getSize(widthMeasureSpec)
                        val height = MeasureSpec.getSize(heightMeasureSpec)
                        setMeasuredDimension(width, height)
                    }
                }
                    .apply {
                        setVideoURI(videoUri)
                        setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.isLooping = true // Loop the video
                            start() // Start playback
                        }
                        layoutParams =
                            android.widget.FrameLayout.LayoutParams(
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
fun ModeSelectionScreenPreview() {
    TuneralphaTheme { ModeSelectionScreen() }
}
