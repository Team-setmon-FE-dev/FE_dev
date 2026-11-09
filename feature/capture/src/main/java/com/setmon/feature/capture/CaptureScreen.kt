package com.setmon.feature.capture

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.setmon.core.designsystem.R
import com.setmon.feature.capture.data.dto.ClipRegisterResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 1. Font & Typography Setup
// ==========================================
val PretendardFontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.W400),
    Font(R.font.pretendard_medium, FontWeight.W500),
    Font(R.font.pretendard_semibold, FontWeight.W600)
)
val PlayfairFontFamily = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.W400)
)

// Color Palette
val DarkGreen = Color(0xFF1E3A2F)
val CreamBackground = Color(0xFFF7F4EC)
val ToastBadgeBackground = Color(0xFFC7B28B)
val MutedText = Color(0xFF757575)
val AccentRed = Color(0xFFD34E36)
val ProgressTrackColor = Color(0xFFDDD2BA)

// ==========================================
// 2. State Machine Enum
// ==========================================
enum class CaptureStep {
    CAPTURE_IDLE,    // Step 1: 대기
    RECORDING,       // Step 2: 녹화 중
    PREVIEW,         // Step 3: 미러링/미리보기 (영상 무한 재생)
    CHOOSE_COVER     // Step 4: 커버 선택
}

// ==========================================
// 3. Main Screen Flow Container (CaptureScreen)
// ==========================================
@SuppressLint("MissingPermission")
@Composable
fun CaptureScreen(
    uiState: CaptureUiState = CaptureUiState.Idle,
    onRecordComplete: (videoFile: File, thumbFile: File, durationMs: Long) -> Unit = { _, _, _ -> },
    onUploadSuccess: (ClipRegisterResponse) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is CaptureUiState.Success -> {
                onUploadSuccess(uiState.response)
            }
            is CaptureUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    fun checkPermissions(ctx: Context): Boolean {
        val cameraGranted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val audioGranted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        return cameraGranted && audioGranted
    }

    var hasPermissions by remember { mutableStateOf(checkPermissions(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
        if (!hasPermissions) {
            Toast.makeText(context, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    if (!hasPermissions) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "카메라 및 마이크 권한이 필요합니다.",
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("권한 요청", color = Color.White)
                }
            }
        }
        return
    }

    var currentStep by remember { mutableStateOf(CaptureStep.CAPTURE_IDLE) }
    var recordDurationSeconds by remember { mutableFloatStateOf(0.0f) }
    var selectedFrameIndex by remember { mutableIntStateOf(2) }
    var capturedVideoFile by remember { mutableStateOf<File?>(null) }
    var extractedCoverFrames by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val recorder = remember {
        Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.HD,
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )
            )
            .build()
    }
    val videoCapture = remember { VideoCapture.withOutput(recorder) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var recordingStartTimeMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    lensFacing,
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                Log.e("CaptureScreen", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    val crossfadeTarget = when (currentStep) {
        CaptureStep.CAPTURE_IDLE, CaptureStep.RECORDING -> CaptureStep.CAPTURE_IDLE
        CaptureStep.PREVIEW -> CaptureStep.PREVIEW
        CaptureStep.CHOOSE_COVER -> CaptureStep.CHOOSE_COVER
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Crossfade(
            targetState = crossfadeTarget,
            animationSpec = tween(durationMillis = 300),
            label = "ScreenTransition"
        ) { step ->
            when (step) {
                CaptureStep.CAPTURE_IDLE, CaptureStep.RECORDING -> {
                    CaptureAndRecordScreen(
                        currentStep = currentStep,
                        recordDurationSeconds = recordDurationSeconds,
                        previewView = previewView,
                        onSwitchCamera = {
                            lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                        },
                        onStartRecord = {
                            try {
                                val cacheDir = context.cacheDir
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
                                val videoFile = File(cacheDir, "CLIP_${timeStamp}.mp4")
                                val outputOptions = FileOutputOptions.Builder(videoFile).build()
                                recordingStartTimeMs = SystemClock.elapsedRealtime()
                                currentStep = CaptureStep.RECORDING

                                val pendingRecording = videoCapture.output.prepareRecording(context, outputOptions)
                                val hasAudioPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                activeRecording = if (hasAudioPermission) {
                                    pendingRecording.withAudioEnabled()
                                } else {
                                    pendingRecording
                                }.start(ContextCompat.getMainExecutor(context)) { event ->
                                    if (event is VideoRecordEvent.Finalize) {
                                        if (!event.hasError()) {
                                            val elapsedMs = SystemClock.elapsedRealtime() - recordingStartTimeMs
                                            // 1초 미만인 경우 (1000ms 미만) 촬영 거부 및 삭제
                                            if (elapsedMs < 1000L) {
                                                videoFile.delete()
                                                Toast.makeText(context, "1초 이상 촬영해주세요.", Toast.LENGTH_SHORT).show()
                                                recordDurationSeconds = 0f
                                                currentStep = CaptureStep.CAPTURE_IDLE
                                            } else {
                                                capturedVideoFile = videoFile
                                                extractedCoverFrames = extractFramesFromVideo(videoFile, count = 7)
                                                currentStep = CaptureStep.PREVIEW
                                            }
                                        } else {
                                            videoFile.delete()
                                            recordDurationSeconds = 0f
                                            currentStep = CaptureStep.CAPTURE_IDLE
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("CaptureScreen", "Recording start failed", e)
                                Toast.makeText(context, "녹화 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                currentStep = CaptureStep.CAPTURE_IDLE
                            }
                        },
                        onRecordingProgress = { recordDurationSeconds = it },
                        onFinishRecord = {
                            try {
                                activeRecording?.stop()
                            } catch (e: Exception) {
                                Log.e("CaptureScreen", "Recording stop failed", e)
                            } finally {
                                activeRecording = null
                            }
                        },
                        onClose = onBack
                    )
                }
                CaptureStep.PREVIEW -> {
                    PreviewScreen(
                        recordDuration = recordDurationSeconds,
                        videoFile = capturedVideoFile,
                        previewBitmap = extractedCoverFrames.getOrNull(selectedFrameIndex),
                        onRetake = {
                            capturedVideoFile?.delete()
                            capturedVideoFile = null
                            extractedCoverFrames = emptyList()
                            recordDurationSeconds = 0f
                            currentStep = CaptureStep.CAPTURE_IDLE
                        },
                        onChooseCover = {
                            currentStep = CaptureStep.CHOOSE_COVER
                        },
                        onUpload = {
                            capturedVideoFile?.let { file ->
                                val totalMs = (recordDurationSeconds * 1000f).toLong().coerceIn(1000L, 2000L)
                                val selectedBitmap = extractedCoverFrames.getOrNull(selectedFrameIndex)
                                    ?: extractedCoverFrames.firstOrNull()
                                val thumbFile = if (selectedBitmap != null) {
                                    saveBitmapToFile(context, selectedBitmap)
                                } else {
                                    File(context.cacheDir, "THUMB_temp.jpg").apply { createNewFile() }
                                }
                                onRecordComplete(file, thumbFile, totalMs)
                            }
                        },
                        onBack = {
                            currentStep = CaptureStep.CAPTURE_IDLE
                        }
                    )
                }
                CaptureStep.CHOOSE_COVER -> {
                    ChooseCoverScreen(
                        selectedIndex = selectedFrameIndex,
                        frames = extractedCoverFrames,
                        onFrameSelected = { selectedFrameIndex = it },
                        onUseCover = {
                            currentStep = CaptureStep.PREVIEW
                        },
                        onBack = {
                            currentStep = CaptureStep.PREVIEW
                        }
                    )
                }
            }
        }

        if (uiState is CaptureUiState.Uploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

// ==========================================
// 4. Capture & Record Screen Component
// ==========================================
@Composable
private fun CaptureAndRecordScreen(
    currentStep: CaptureStep,
    recordDurationSeconds: Float,
    previewView: PreviewView,
    onSwitchCamera: () -> Unit,
    onStartRecord: () -> Unit,
    onRecordingProgress: (Float) -> Unit,
    onFinishRecord: () -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) }
    val currentHourText = remember { SimpleDateFormat("HH:00", Locale.KOREA).format(Date()) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentHourText,
                        fontFamily = PlayfairFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W400,
                        color = Color.White
                    )
                    Text(
                        text = "this hour's moment",
                        fontFamily = PretendardFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W400,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (currentStep == CaptureStep.CAPTURE_IDLE) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ToastBadgeBackground.copy(alpha = 0.85f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Already posted at $currentHourText   uploading will replace it",
                        fontFamily = PretendardFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W400,
                        color = Color(0xFF2C2216)
                    )
                }
            } else if (currentStep == CaptureStep.RECORDING) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format(Locale.KOREA, "%.1f s / 2.0 s", recordDurationSeconds),
                            fontFamily = PretendardFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W500,
                            color = Color.White
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (currentStep == CaptureStep.RECORDING) "Release to finish" else "Hold to record  2 seconds",
                fontFamily = PretendardFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.size(48.dp))
                RecordButton(
                    isRecording = currentStep == CaptureStep.RECORDING,
                    progress = (recordDurationSeconds / 2.0f).coerceIn(0f, 1f),
                    onPressStart = {
                        timerJob?.cancel()
                        onStartRecord()
                        timerJob = coroutineScope.launch {
                            var elapsed = 0f
                            val interval = 0.05f
                            while (elapsed < 2.0f) {
                                delay((interval * 1000).toLong())
                                elapsed += interval
                                onRecordingProgress(elapsed)
                            }
                            onFinishRecord()
                        }
                    },
                    onPressEnd = {
                        timerJob?.cancel()
                        onFinishRecord()
                    }
                )
                Spacer(modifier = Modifier.width(28.dp))
                IconButton(
                    onClick = onSwitchCamera,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. Canvas Record Button Component
// ==========================================
@Composable
private fun RecordButton(
    isRecording: Boolean,
    progress: Float,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 50),
        label = "ArcProgress"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(96.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressStart()
                        tryAwaitRelease()
                        onPressEnd()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val strokeWidth = 5.dp.toPx()
            val radiusOffset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(
                color = if (isRecording) ProgressTrackColor.copy(alpha = 0.5f) else Color.White,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(radiusOffset, radiusOffset),
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            if (isRecording) {
                drawArc(
                    color = Color.Black.copy(alpha = 0.85f),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(radiusOffset, radiusOffset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(if (isRecording) 36.dp else 68.dp)
                .clip(if (isRecording) RoundedCornerShape(8.dp) else CircleShape)
                .background(if (isRecording) AccentRed else DarkGreen)
        )
    }
}

// ==========================================
// 6. Preview Screen Component (비디오 무한 루핑 재생)
// ==========================================
@Composable
private fun PreviewScreen(
    recordDuration: Float,
    videoFile: File?,
    previewBitmap: Bitmap?,
    onRetake: () -> Unit,
    onChooseCover: () -> Unit,
    onUpload: () -> Unit,
    onBack: () -> Unit
) {
    val currentHourText = remember { SimpleDateFormat("HH:00", Locale.KOREA).format(Date()) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (videoFile != null && videoFile.exists()) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoPath(videoFile.absolutePath)
                        setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.isLooping = true
                            // 원본 비율에 맞춰 확대(Crop) 없이 맞춤
                            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                            mediaPlayer.start()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (previewBitmap != null) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = "Preview Video Clip",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = ColorPainter(Color(0xFF382E2B)),
                contentDescription = "Preview Video Clip",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = String.format(Locale.KOREA, "Looping   %.1f s", recordDuration),
                    fontFamily = PretendardFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = Color.White
                )
            }
        }
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = CreamBackground),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Today   $currentHourText",
                            fontFamily = PretendardFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W600,
                            color = Color(0xFF1C1C1C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Replaces your earlier $currentHourText clip",
                            fontFamily = PretendardFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W400,
                            color = MutedText
                        )
                    }
                    Text(
                        text = "Choose cover",
                        fontFamily = PretendardFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W500,
                        color = DarkGreen,
                        modifier = Modifier.clickable { onChooseCover() }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetake,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1C1C1C))
                    ) {
                        Text(
                            text = "Retake",
                            fontFamily = PretendardFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                    Button(
                        onClick = onUpload,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Upload",
                            fontFamily = PretendardFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W500,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. Choose Cover Screen Component
// ==========================================
@Composable
private fun ChooseCoverScreen(
    selectedIndex: Int,
    frames: List<Bitmap>,
    onFrameSelected: (Int) -> Unit,
    onUseCover: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(top = 44.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1C1C1C)
                )
            }
            Text(
                text = "Choose cover",
                fontFamily = PretendardFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.W600,
                color = Color(0xFF1C1C1C),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(0.72f)
                .shadow(12.dp, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF382E2B))
        ) {
            val selectedBitmap = frames.getOrNull(selectedIndex)
            if (selectedBitmap != null) {
                Image(
                    bitmap = selectedBitmap.asImageBitmap(),
                    contentDescription = "Cover Selected Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = ColorPainter(Color(0xFF4A3B37)),
                    contentDescription = "Cover Selected Frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ToastBadgeBackground.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Shown on your calendar",
                    fontFamily = PretendardFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W400,
                    color = Color(0xFF2C2216)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            repeat(7) { index ->
                val isSelected = index == selectedIndex
                val frameBitmap = frames.getOrNull(index)
                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF5A4A45))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) DarkGreen else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onFrameSelected(index) }
                ) {
                    if (frameBitmap != null) {
                        Image(
                            bitmap = frameBitmap.asImageBitmap(),
                            contentDescription = "Frame $index",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = ColorPainter(Color(0xFF4A3B37)),
                            contentDescription = "Frame $index",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Slide along the clip to pick the frame\nthat appears in your calendar cell",
            fontFamily = PretendardFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.W400,
            color = MutedText,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onUseCover,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
        ) {
            Text(
                text = "Use this cover",
                fontFamily = PretendardFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.W500,
                color = Color.White
            )
        }
    }
}

// ==========================================
// 8. Helper Functions
// ==========================================
private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
    val file = File(context.cacheDir, "THUMB_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file
}

private fun extractFramesFromVideo(videoFile: File, count: Int = 7): List<Bitmap> {
    val retriever = MediaMetadataRetriever()
    val frames = mutableListOf<Bitmap>()
    try {
        retriever.setDataSource(videoFile.absolutePath)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toLongOrNull() ?: 0L
        val durationUs = durationMs * 1000L
        val stepUs = if (count > 1) durationUs / (count - 1) else 0L
        for (i in 0 until count) {
            val timeUs = stepUs * i
            val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(timeUs)
            bitmap?.let { frames.add(it) }
        }
    } catch (e: Exception) {
        Log.e("CaptureScreen", "Frame extraction failed", e)
    } finally {
        try {
            retriever.release()
        } catch (ignored: Exception) {}
    }
    return frames
}