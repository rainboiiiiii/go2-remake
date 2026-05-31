package com.go2super.server.websocket;

import com.go2super.logger.BotLogger;
import com.go2super.server.PacketFraming;
import com.go2super.service.GameNetworkService;
import org.apache.mina.core.buffer.IoBuffer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;

@Component
@ConditionalOnExpression("'${application.game.transport:both}'.contains('websocket')")
public class GameWebSocketHandler extends BinaryWebSocketHandler {

    private final GameNetworkService gameNetworkService;
    private final GameConnectionManager connectionManager;

    public GameWebSocketHandler(GameNetworkService gameNetworkService, GameConnectionManager connectionManager) {
        this.gameNetworkService = gameNetworkService;
        this.connectionManager = connectionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        WebSocketSmartServer connection = new WebSocketSmartServer(session);
        connectionManager.register(session, connection);
        BotLogger.thread("WebSocket game connection opened: " + session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        WebSocketSmartServer connection = connectionManager.get(session);

        if (connection == null) {
            return;
        }

        ByteBuffer payloadBuffer = message.getPayload();
        byte[] payload = new byte[payloadBuffer.remaining()];
        payloadBuffer.get(payload);

        IoBuffer realBuffer = PacketFraming.toIoBuffer(payload, payload.length);

        try {
            PacketFraming.processBuffer(
                    realBuffer,
                    gameNetworkService.getPacketRouter(),
                    null,
                    connection,
                    true
            );
        } catch (Exception e) {
            BotLogger.log("WebSocket packet error for session " + session.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        connectionManager.unregister(session);
        BotLogger.thread("WebSocket game connection closed: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        connectionManager.unregister(session);
        BotLogger.log("WebSocket transport error for session " + session.getId() + ": " + exception.getMessage());
    }

}
