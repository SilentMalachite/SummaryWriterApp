package services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import models.TextMessage
import java.util.concurrent.ConcurrentLinkedQueue

class TextService {
    private val _latestText = MutableStateFlow(TextMessage())
    val latestText: StateFlow<TextMessage> = _latestText.asStateFlow()

    private val _textHistory = MutableStateFlow<List<TextMessage>>(emptyList())
    val textHistory: StateFlow<List<TextMessage>> = _textHistory.asStateFlow()
    
    private val historyQueue = ConcurrentLinkedQueue<TextMessage>()
    private val maxHistorySize = 1000

    fun updateText(message: TextMessage) {
        _latestText.value = message
        
        // スレッドセーフな履歴管理
        historyQueue.offer(message)
        if (historyQueue.size > maxHistorySize) {
            historyQueue.poll()
        }
        
        _textHistory.value = historyQueue.toList()
    }

    fun getLatestText(): TextMessage = _latestText.value

    fun getTextHistory(): List<TextMessage> = _textHistory.value

    fun clearHistory() {
        historyQueue.clear()
        _textHistory.value = emptyList()
        _latestText.value = TextMessage()
    }
}
