package services

import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.TextMessage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class WebSocketService {
    private val connections = ConcurrentHashMap<String, DefaultWebSocketSession>()
    private val connectionMutex = Mutex()
    private val messageCounter = AtomicInteger(0)

    suspend fun addConnection(sessionId: String, session: DefaultWebSocketSession) = connectionMutex.withLock {
        connections[sessionId] = session
        println("WebSocket接続追加: $sessionId (合計: ${connections.size})")
    }

    suspend fun removeConnection(sessionId: String) = connectionMutex.withLock {
        connections.remove(sessionId)
        println("WebSocket接続削除: $sessionId (残り: ${connections.size})")
    }

    suspend fun sendText(message: TextMessage) {
        val jsonMessage = Json.encodeToString(message)
        broadcast(jsonMessage)
        messageCounter.incrementAndGet()
    }

    suspend fun broadcast(message: String) = connectionMutex.withLock {
        val disconnectedSessions = mutableListOf<String>()
        
        connections.forEach { (sessionId, session) ->
            try {
                session.send(Frame.Text(message))
            } catch (e: Exception) {
                println("セッション ${sessionId} へのメッセージ送信失敗: ${e.message}")
                disconnectedSessions.add(sessionId)
            }
        }
        
        // 切断されたセッションをクリーンアップ
        disconnectedSessions.forEach { 
            connections.remove(it)
            println("切断セッションをクリーンアップ: $it")
        }
    }

    fun getActiveConnectionsCount(): Int = connections.size
    
    fun getTotalMessagesSent(): Int = messageCounter.get()
    
    fun getConnectionIds(): List<String> = connections.keys.toList()
}
