package server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.TextMessage
import services.TextService
import services.WebSocketService
import java.time.Duration
import java.util.*

fun Application.configureTextServer(
    textService: TextService,
    webSocketService: WebSocketService
) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    routing {
        webSocket("/textHub") {
            val sessionId = UUID.randomUUID().toString()
            webSocketService.addConnection(sessionId, this)
            
            println("新規WebSocket接続: $sessionId")

            try {
                // 接続確認メッセージを送信
                send(Frame.Text(Json.encodeToString(mapOf(
                    "type" to "connected",
                    "sessionId" to sessionId,
                    "message" to "接続成功"
                ))))

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            try {
                                val message = Json.decodeFromString<TextMessage>(receivedText)
                                textService.updateText(message)
                                webSocketService.broadcast(Json.encodeToString(message))
                            } catch (e: Exception) {
                                println("メッセージ解析エラー: ${e.message}")
                                send(Frame.Text(Json.encodeToString(mapOf(
                                    "type" to "error",
                                    "message" to "Invalid message format: ${e.message}"
                                ))))
                            }
                        }
                        is Frame.Close -> {
                            println("WebSocket接続クローズ要求: $sessionId")
                            break
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                println("WebSocket error: ${e.message}")
                e.printStackTrace()
            } finally {
                webSocketService.removeConnection(sessionId)
                println("WebSocket接続終了: $sessionId")
            }
        }

        get("/text") {
            val latestText = textService.getLatestText().text
            call.respondText(latestText, ContentType.Text.Plain, HttpStatusCode.OK)
        }

        get("/api/text") {
            val latestText = textService.getLatestText()
            call.respond(HttpStatusCode.OK, latestText)
        }

        get("/api/history") {
            val history = textService.getTextHistory()
            call.respond(HttpStatusCode.OK, history)
        }
        
        get("/api/stats") {
            val stats = mapOf(
                "activeConnections" to webSocketService.getActiveConnectionsCount(),
                "totalMessages" to webSocketService.getTotalMessagesSent(),
                "historySize" to textService.getTextHistory().size,
                "connectionIds" to webSocketService.getConnectionIds()
            )
            call.respond(HttpStatusCode.OK, stats)
        }

        get("/") {
            call.respondText(
                """
                <!DOCTYPE html>
                <html lang="ja">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>要約筆記 - Summary Writer</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh;
                            padding: 20px;
                        }
                        .container {
                            max-width: 900px;
                            margin: 0 auto;
                            background: white;
                            padding: 30px;
                            border-radius: 12px;
                            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                        }
                        h1 {
                            color: #333;
                            margin-bottom: 20px;
                            font-size: 2em;
                        }
                        #textDisplay {
                            font-size: 28px;
                            min-height: 150px;
                            padding: 30px;
                            background: #f8f9fa;
                            border-radius: 8px;
                            margin: 20px 0;
                            border: 3px solid #667eea;
                            line-height: 1.6;
                            word-wrap: break-word;
                        }
                        .status {
                            display: inline-block;
                            padding: 8px 16px;
                            border-radius: 20px;
                            font-size: 14px;
                            font-weight: 600;
                            margin-bottom: 15px;
                        }
                        .connected {
                            background: #d4edda;
                            color: #155724;
                        }
                        .disconnected {
                            background: #f8d7da;
                            color: #721c24;
                        }
                        .info {
                            color: #666;
                            font-size: 14px;
                            margin-top: 20px;
                            padding: 15px;
                            background: #e9ecef;
                            border-radius: 6px;
                        }
                        @media (max-width: 600px) {
                            #textDisplay {
                                font-size: 20px;
                                padding: 20px;
                            }
                            h1 {
                                font-size: 1.5em;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>📝 要約筆記表示</h1>
                        <div class="status" id="status">接続中...</div>
                        <div id="textDisplay">テキストを待っています...</div>
                        <div class="info">
                            <strong>接続情報:</strong><br>
                            WebSocket: ws://[host]/textHub<br>
                            セッションID: <span id="sessionId">-</span>
                        </div>
                    </div>
                    <script>
                        const ws = new WebSocket('ws://' + window.location.host + '/textHub');
                        const textDisplay = document.getElementById('textDisplay');
                        const status = document.getElementById('status');
                        const sessionIdEl = document.getElementById('sessionId');
                        
                        ws.onopen = function() {
                            status.textContent = '✓ 接続済み';
                            status.className = 'status connected';
                        };
                        
                        ws.onmessage = function(event) {
                            try {
                                const data = JSON.parse(event.data);
                                
                                if (data.type === 'connected') {
                                    sessionIdEl.textContent = data.sessionId;
                                } else if (data.text !== undefined) {
                                    textDisplay.textContent = data.text;
                                    // アニメーション効果
                                    textDisplay.style.animation = 'none';
                                    setTimeout(() => {
                                        textDisplay.style.animation = 'fadeIn 0.3s';
                                    }, 10);
                                }
                            } catch (e) {
                                console.error('Error parsing message:', e);
                            }
                        };
                        
                        ws.onerror = function(error) {
                            status.textContent = '✗ 接続エラー';
                            status.className = 'status disconnected';
                            console.error('WebSocket error:', error);
                        };
                        
                        ws.onclose = function() {
                            status.textContent = '✗ 切断されました';
                            status.className = 'status disconnected';
                        };
                    </script>
                    <style>
                        @keyframes fadeIn {
                            from { opacity: 0.5; }
                            to { opacity: 1; }
                        }
                    </style>
                </body>
                </html>
                """.trimIndent(),
                ContentType.Text.Html
            )
        }
    }
}
