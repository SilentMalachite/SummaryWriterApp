package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import models.TextMessage
import services.WebSocketService
import java.time.Instant
import java.util.*

@OptIn(FlowPreview::class)
@Composable
fun TextInputView(
    webSocketService: WebSocketService,
    modifier: Modifier = Modifier
) {
    var textState by remember { mutableStateOf("") }
    val userId = remember { UUID.randomUUID().toString() }

    LaunchedEffect(textState) {
        snapshotFlow { textState }
            .debounce(500)
            .onEach { text ->
                if (text.isNotEmpty()) {
                    val message = TextMessage(
                        text = text,
                        timestamp = Instant.now().toString(),
                        userId = userId
                    )
                    webSocketService.sendText(message)
                }
            }
            .launchIn(this)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "テキスト入力",
            style = MaterialTheme.typography.h6
        )

        TextField(
            value = textState,
            onValueChange = { textState = it },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            placeholder = { Text("ここにテキストを入力してください") },
            maxLines = 5
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "接続数: ${webSocketService.getActiveConnectionsCount()}",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(end = 8.dp)
            )
            Button(onClick = { textState = "" }) {
                Text("クリア")
            }
        }
    }
}
