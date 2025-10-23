import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import server.configureTextServer
import services.CameraService
import services.TextService
import services.WebSocketService
import ui.CameraOverlayView
import ui.TextDisplayView
import ui.TextInputView

fun main() {
    val textService = TextService()
    val webSocketService = WebSocketService()
    val cameraService = CameraService()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureTextServer(textService, webSocketService)
    }.start(wait = false)

    println("サーバーが起動しました: http://localhost:8080")
    println("WebSocket: ws://localhost:8080/textHub")

    application {
        val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
        
        Window(
            onCloseRequest = {
                try {
                    cameraService.close()
                } catch (e: Exception) {
                    println("クリーンアップエラー: ${e.message}")
                }
                exitApplication()
            },
            title = "要約筆記アプリ - Summary Writer",
            state = windowState
        ) {
            App(textService, webSocketService, cameraService)
        }
    }
}

@Composable
@Preview
fun App(
    textService: TextService = TextService(),
    webSocketService: WebSocketService = WebSocketService(),
    cameraService: CameraService = CameraService()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var fontSize by remember { mutableStateOf(16f) }
    var highContrast by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("要約筆記アプリ - Summary Writer") }
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("入力") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("字幕表示") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("カメラ表示") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("設定") }
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> TextInputView(webSocketService)
                    1 -> TextDisplayView(
                        textService = textService,
                        fontSize = fontSize,
                        highContrast = highContrast
                    )
                    2 -> CameraOverlayView(
                        cameraService = cameraService,
                        textService = textService,
                        fontSize = fontSize
                    )
                    3 -> SettingsView(
                        fontSize = fontSize,
                        onFontSizeChange = { fontSize = it },
                        highContrast = highContrast,
                        onHighContrastChange = { highContrast = it },
                        textService = textService
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsView(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    highContrast: Boolean,
    onHighContrastChange: (Boolean) -> Unit,
    textService: TextService
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "設定",
            style = MaterialTheme.typography.h5
        )

        Divider()

        Text("フォントサイズ: ${fontSize.toInt()}pt")
        Slider(
            value = fontSize,
            onValueChange = onFontSizeChange,
            valueRange = 10f..32f,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("高コントラストモード")
            Switch(
                checked = highContrast,
                onCheckedChange = onHighContrastChange
            )
        }

        Divider()

        Text(
            text = "サーバー情報",
            style = MaterialTheme.typography.h6
        )
        Text("HTTP: http://localhost:8080")
        Text("WebSocket: ws://localhost:8080/textHub")
        Text("API: http://localhost:8080/api/text")

        Divider()

        Button(
            onClick = { textService.clearHistory() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("履歴をクリア")
        }
    }
}
