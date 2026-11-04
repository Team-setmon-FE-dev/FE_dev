package com.setmon.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.theme.SetmonTheme

/**
 * 1주차 뼈대용 빈 화면. 화면 ID와 이름, 이동 버튼만 보여준다.
 * 실제 화면을 만들면 각 feature에서 이 컴포넌트 대신 진짜 UI로 바꾼다.
 */
@Composable
fun PlaceholderScreen(
    screenId: String,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = screenId, style = MaterialTheme.typography.labelLarge)
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            if (description != null) {
                Spacer(Modifier.height(8.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = actions,
            )
        }
    }
}

/** 빈 화면에서 다른 화면으로 이동해보는 버튼 */
@Composable
fun PlaceholderButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    SetmonTheme {
        PlaceholderScreen(screenId = "S-00", title = "미리보기", description = "설명") {
            PlaceholderButton(text = "다음 화면") {}
        }
    }
}
