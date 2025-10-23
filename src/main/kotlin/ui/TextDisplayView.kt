package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.TextMessage
import services.TextService

@Composable
fun TextDisplayView(
    textService: TextService,
    modifier: Modifier = Modifier,
    fontSize: Float = 16f,
    highContrast: Boolean = false
) {
    val textHistory by textService.textHistory.collectAsState()
    val latestText by textService.latestText.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(textHistory.size) {
        if (textHistory.isNotEmpty()) {
            listState.animateScrollToItem(textHistory.size - 1)
        }
    }

    val backgroundColor = if (highContrast) Color.Black else MaterialTheme.colors.surface
    val textColor = if (highContrast) Color.White else MaterialTheme.colors.onSurface

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "字幕表示",
                style = MaterialTheme.typography.h6,
                color = textColor
            )
            Text(
                text = "履歴: ${textHistory.size}件",
                style = MaterialTheme.typography.caption,
                color = textColor
            )
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = if (highContrast) Color.White else MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        )

        if (latestText.text.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                backgroundColor = if (highContrast) Color.DarkGray else MaterialTheme.colors.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = latestText.text,
                    modifier = Modifier.padding(16.dp),
                    fontSize = (fontSize * 1.2f).sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(textHistory) { message ->
                TextMessageItem(
                    message = message,
                    fontSize = fontSize,
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
private fun TextMessageItem(
    message: TextMessage,
    fontSize: Float,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = message.text,
            fontSize = fontSize.sp,
            color = textColor
        )
        Text(
            text = message.timestamp,
            style = MaterialTheme.typography.caption,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}
