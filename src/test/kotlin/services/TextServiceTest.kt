package services

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import models.TextMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class TextServiceTest {
    private lateinit var textService: TextService

    @BeforeEach
    fun setup() {
        textService = TextService()
    }

    @Test
    fun `updateText should update latest text`() = runTest {
        val message = TextMessage(
            text = "テストメッセージ",
            timestamp = Instant.now().toString(),
            userId = "test-user"
        )

        textService.updateText(message)

        val latestText = textService.getLatestText()
        assertEquals("テストメッセージ", latestText.text)
        assertEquals("test-user", latestText.userId)
    }

    @Test
    fun `updateText should add to history`() = runTest {
        val message1 = TextMessage(text = "メッセージ1", timestamp = Instant.now().toString(), userId = "user1")
        val message2 = TextMessage(text = "メッセージ2", timestamp = Instant.now().toString(), userId = "user2")

        textService.updateText(message1)
        textService.updateText(message2)

        val history = textService.getTextHistory()
        assertEquals(2, history.size)
        assertEquals("メッセージ1", history[0].text)
        assertEquals("メッセージ2", history[1].text)
    }

    @Test
    fun `history should not exceed 1000 items`() = runTest {
        repeat(1100) { index ->
            textService.updateText(
                TextMessage(
                    text = "メッセージ $index",
                    timestamp = Instant.now().toString(),
                    userId = "user"
                )
            )
        }

        val history = textService.getTextHistory()
        assertEquals(1000, history.size)
        assertEquals("メッセージ 100", history[0].text)
        assertEquals("メッセージ 1099", history[999].text)
    }

    @Test
    fun `clearHistory should clear all messages`() = runTest {
        val message = TextMessage(text = "テスト", timestamp = Instant.now().toString(), userId = "user")
        textService.updateText(message)

        textService.clearHistory()

        val history = textService.getTextHistory()
        val latestText = textService.getLatestText()
        
        assertEquals(0, history.size)
        assertEquals("", latestText.text)
    }

    @Test
    fun `latestText flow should emit updates`() = runTest {
        val message = TextMessage(text = "新しいメッセージ", timestamp = Instant.now().toString(), userId = "user")
        
        textService.updateText(message)
        
        val emittedMessage = textService.latestText.first()
        assertEquals("新しいメッセージ", emittedMessage.text)
    }
}
