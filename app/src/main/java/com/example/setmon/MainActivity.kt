package com.example.setmon

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.setmon.ui.theme.SetmonTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SetmonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CameraTestScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun CameraTestScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. 카메라 & 마이크 권한 요청
    var hasPermissions by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    if (!hasPermissions) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("카메라 및 마이크 권한을 허용해 주세요.")
        }
        return
    }

    // 2. CameraX 및 녹화 관련 상태값 초기화
    val previewView = remember { PreviewView(context) }

    // 전면 / 후면 카메라 상태 관리 (기본값: 후면)
    var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    val recorder = remember {
        Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
    }
    val videoCapture = remember { VideoCapture.withOutput(recorder) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var recordingStartTime by remember { mutableLongStateOf(0L) }
    var thumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRecordingUI by remember { mutableStateOf(false) }

    val handler = remember { Handler(Looper.getMainLooper()) }
    val stopRunnable = remember {
        Runnable {
            activeRecording?.stop()
            activeRecording = null
            isRecordingUI = false
            Toast.makeText(context, "2초 자동 촬영 완료!", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. 카메라 바인딩
    LaunchedEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    lensFacing, // 전면 또는 후면 선택
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                Log.e("CameraTest", "카메라 바인딩 실패", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // 4. UI 레이아웃
    Box(modifier = modifier.fillMaxSize()) {
        // 뷰파인더
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // 우측 상단: 전면/후면 카메라 전환 버튼
        Button(
            onClick = {
                // 전면 ↔ 후면 스위칭
                lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
        ) {
            Text(if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) "전면 전환" else "후면 전환")
        }

        // 하단 컨트롤 영역
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 추출된 썸네일 미리보기
            thumbnailBitmap?.let { bitmap ->
                Text("추출된 첫 프레임 썸네일:", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Thumbnail Preview",
                    modifier = Modifier
                        .size(90.dp, 130.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = if (isRecordingUI) "녹화 중... (손을 떼면 완료)" else "버튼을 길게 눌러 1~2초 촬영",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 홀드 녹화 버튼
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isRecordingUI) Color.Red else Color.White)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                                val outputFile = File(moviesDir, "test_clip_${System.currentTimeMillis()}.mp4")
                                Log.d("FilePath", "비디오 저장 절대경로: ${outputFile.absolutePath}")

                                val outputOptions = FileOutputOptions.Builder(outputFile).build()

                                recordingStartTime = System.currentTimeMillis()
                                isRecordingUI = true

                                // 녹화 시작
                                activeRecording = videoCapture.output
                                    .prepareRecording(context, outputOptions)
                                    .withAudioEnabled()
                                    .start(ContextCompat.getMainExecutor(context)) { event ->
                                        if (event is VideoRecordEvent.Finalize) {
                                            isRecordingUI = false
                                            if (!event.hasError()) {
                                                val duration = System.currentTimeMillis() - recordingStartTime
                                                if (duration < 1000) { // 1초 미만 취소
                                                    outputFile.delete()
                                                    Toast.makeText(context, "1초 이상 찍어주세요", Toast.LENGTH_SHORT).show()
                                                } else { // 1~2초 성공시 썸네일 추출
                                                    thumbnailBitmap = extractFirstFrame(context, outputFile)
                                                    Toast.makeText(context, "저장 완료: ${outputFile.name}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }

                                // 2초 초과 시 자동 중지 타이머
                                handler.postDelayed(stopRunnable, 2000)

                                // 손을 뗄 때까지 대기
                                tryAwaitRelease()

                                // 손을 떼었을 때 녹화 종료
                                handler.removeCallbacks(stopRunnable)
                                activeRecording?.stop()
                                activeRecording = null
                                isRecordingUI = false
                            }
                        )
                    }
            )
        }
    }
}

// 첫 프레임을 Bitmap으로 추출하여 JPEG로 저장하는 함수
private fun extractFirstFrame(context: Context, videoFile: File): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(videoFile.absolutePath)
        val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

        if (bitmap != null) {
            val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            val thumbFile = File(moviesDir, "thumb_${videoFile.nameWithoutExtension}.jpg")
            FileOutputStream(thumbFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Log.d("FilePath", "썸네일 저장 절대경로: ${thumbFile.absolutePath}")
        }
        bitmap
    } catch (e: Exception) {
        Log.e("FilePath", "썸네일 추출 실패", e)
        null
    } finally {
        retriever.release()
    }
}