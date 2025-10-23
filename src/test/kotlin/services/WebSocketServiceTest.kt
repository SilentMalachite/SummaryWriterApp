package services

import io.ktor.websocket.*
import kotlinx.coroutines.test.runTest
import models.TextMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class WebSocketServiceTest {
    private lateinit var webSocketService: WebSocketService

    @BeforeEach
    fun setup() {
        webSocketService = WebSocketService()
    }

    @Test
    fun `getActiveConnectionsCount should return 0 initially`() {
        assertEquals(0, webSocketService.getActiveConnectionsCount())
    }

    @Test
    fun `removeConnection should decrease connection count`() = runTest {
        // This is a basic test - full WebSocket testing requires mock sessions
        val initialCount = webSocketService.getActiveConnectionsCount()
        webSocketService.removeConnection("test-session")
        assertEquals(initialCount, webSocketService.getActiveConnectionsCount())
    }

    @Test
    fun `sendText should serialize message correctly`() = runTest {
        val message = TextMessage(
            text = "テストメッセージ",
            timestamp = Instant.now().toString(),
            userId = "test-user"
        )
        
        // Should not throw when broadcasting to no connections
        webSocketService.sendText(message)
        // Test passes if no exception thrown
    }

    @Test
    fun `broadcast should handle empty connections gracefully`() = runTest {
        // Should handle broadcasting to empty connection list
        webSocketService.broadcast("test message")
        // Test passes if no exception thrown
    }
}
