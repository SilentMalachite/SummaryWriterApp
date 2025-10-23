package models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class TextMessageTest {

    @Test
    fun `TextMessage should serialize to JSON correctly`() {
        val timestamp = Instant.now().toString()
        val message = TextMessage(
            text = "テストメッセージ",
            timestamp = timestamp,
            userId = "test-user-123"
        )

        val json = Json.encodeToString(message)

        assertTrue(json.contains("\"text\":\"テストメッセージ\""))
        assertTrue(json.contains("\"userId\":\"test-user-123\""))
        assertTrue(json.contains("\"timestamp\":\"$timestamp\""))
    }

    @Test
    fun `TextMessage should deserialize from JSON correctly`() {
        val timestamp = Instant.now().toString()
        val json = """
            {
                "text": "テストメッセージ",
                "timestamp": "$timestamp",
                "userId": "test-user-123"
            }
        """.trimIndent()

        val message = Json.decodeFromString<TextMessage>(json)

        assertEquals("テストメッセージ", message.text)
        assertEquals(timestamp, message.timestamp)
        assertEquals("test-user-123", message.userId)
    }

    @Test
    fun `TextMessage should have default values`() {
        val message = TextMessage()

        assertEquals("", message.text)
        assertEquals("", message.timestamp)
        assertEquals("", message.userId)
    }

    @Test
    fun `TextMessage should allow partial initialization`() {
        val message = TextMessage(text = "部分的なメッセージ")

        assertEquals("部分的なメッセージ", message.text)
        assertEquals("", message.timestamp)
        assertEquals("", message.userId)
    }

    @Test
    fun `TextMessage should handle empty strings`() {
        val message = TextMessage(
            text = "",
            timestamp = "",
            userId = ""
        )

        val json = Json.encodeToString(message)
        val decoded = Json.decodeFromString<TextMessage>(json)

        assertEquals("", decoded.text)
        assertEquals("", decoded.timestamp)
        assertEquals("", decoded.userId)
    }

    @Test
    fun `TextMessage should handle special characters`() {
        val message = TextMessage(
            text = "特殊文字: \"quotes\" \n改行 \t タブ",
            timestamp = Instant.now().toString(),
            userId = "user-123"
        )

        val json = Json.encodeToString(message)
        val decoded = Json.decodeFromString<TextMessage>(json)

        assertEquals(message.text, decoded.text)
        assertEquals(message.timestamp, decoded.timestamp)
        assertEquals(message.userId, decoded.userId)
    }
}
