package com.setmon.feature.auth.signup

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.component.SetmonButton
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.feature.auth.R

/**
 * 가입 직후 권한 안내 (피그마 `S-02 · Permissions`).
 * [계속]을 누르면 카메라·마이크·알림 권한을 한 번에 요청하고, 허용 여부와 관계없이 달력으로 간다.
 * 거부했을 때 촬영 버튼에서 다시 안내하는 흐름은 3주차 작업.
 */
@Composable
internal fun PermissionScreen(onDone: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { onDone() },
    )
    PermissionContent(onContinue = { launcher.launch(requiredPermissions()) })
}

private fun requiredPermissions(): Array<String> = buildList {
    add(Manifest.permission.CAMERA)
    add(Manifest.permission.RECORD_AUDIO)
    // 알림 권한은 Android 13(API 33)부터 따로 요청한다
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
}.toTypedArray()

@Composable
private fun PermissionContent(onContinue: () -> Unit) {
    val colors = SetmonTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = 36.dp),
    ) {
        Spacer(Modifier.height(136.dp))
        Text(stringResource(R.string.permission_title), style = SetmonTheme.typography.title, color = colors.textPrimary)
        Spacer(Modifier.height(32.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(R.string.permission_camera, R.string.permission_microphone, R.string.permission_notification).forEach {
                Text(stringResource(it), style = SetmonTheme.typography.body, color = colors.textSecondary)
            }
        }
        Spacer(Modifier.weight(1f))
        SetmonButton(
            text = stringResource(R.string.permission_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Preview(showBackground = true, heightDp = 917, widthDp = 412)
@Composable
private fun PermissionContentPreview() {
    SetmonTheme { PermissionContent(onContinue = {}) }
}
