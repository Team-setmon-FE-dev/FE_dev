package com.setmon.feature.day

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setmon.core.designsystem.component.PlaceholderButton
import com.setmon.core.designsystem.component.PlaceholderScreen
import com.setmon.core.designsystem.component.SetmonButton
import com.setmon.core.designsystem.theme.SetmonTheme

/** S-05 하루 상세. 플레이어·타임라인은 3주차, 지금은 "지금 만들기" 버튼만 실제로 동작한다 */
@Composable
internal fun DayScreen(
    userId: Long,
    date: String,
    readOnly: Boolean,
    onBack: () -> Unit,
    viewModel: DayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlaceholderScreen(
        screenId = "S-05",
        title = "하루 상세",
        description = "userId = $userId, date = $date, readOnly = $readOnly",
    ) {
        if (uiState.canMakeVlog) {
            SetmonButton(
                text = stringResource(R.string.day_make_vlog),
                onClick = viewModel::makeVlog,
                loading = uiState.isRequesting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        uiState.message?.let { message ->
            Text(
                text = stringResource(message),
                style = SetmonTheme.typography.label,
                color = if (uiState.messageIsError) SetmonTheme.colors.error else SetmonTheme.colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PlaceholderButton(text = "뒤로", onClick = onBack)
    }
}
