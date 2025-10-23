package models

import kotlinx.serialization.Serializable

@Serializable
data class TextMessage(
    val text: String = "",
    val timestamp: String = "",
    val userId: String = ""
)
