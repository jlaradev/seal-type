package com.jlaradev.sealtype.controller

import com.jlaradev.sealtype.dto.websocket.*
import com.jlaradev.sealtype.service.WebSocketService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class ChatWebSocketController(private val webSocketService: WebSocketService) {

    // Procesa nuevos mensajes: /app/chat/{chatId}/message
    @MessageMapping("/chat/{chatId}/message")
    fun handleNewMessage(
        @DestinationVariable chatId: UUID,
        @Payload request: WebSocketMessageRequest
    ) {
        webSocketService.handleNewMessage(chatId, request)
    }

    // Procesa estado de escritura: /app/chat/{chatId}/typing
    @MessageMapping("/chat/{chatId}/typing")
    fun handleTypingStatus(
        @DestinationVariable chatId: UUID,
        @Payload request: TypingStatusRequest
    ) {
        webSocketService.handleTypingStatus(chatId, request)
    }

    // Procesa cambio de estado online/offline: /app/user/status
    @MessageMapping("/user/status")
    fun handleUserStatusChange(@Payload request: UserStatusRequest) {
        webSocketService.handleUserStatusChange(request)
    }
}