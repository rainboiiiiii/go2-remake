package com.go2super.server.websocket;

import com.go2super.service.PacketService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableScheduling
@ConditionalOnExpression("'${application.game.transport:both}'.contains('websocket')")
public class GameConnectionManager {

    private final Map<String, WebSocketSmartServer> connections = new ConcurrentHashMap<>();

    public void register(WebSocketSession session, WebSocketSmartServer connection) {
        connections.put(session.getId(), connection);
    }

    public void unregister(WebSocketSession session) {
        WebSocketSmartServer connection = connections.remove(session.getId());
        if (connection != null) {
            connection.close();
        }
    }

    public WebSocketSmartServer get(WebSocketSession session) {
        return connections.get(session.getId());
    }

    @Scheduled(fixedDelayString = "${application.game.interval:100}")
    public void enforceTimeouts() {
        int timeout = PacketService.getInstance().getTimeout();

        connections.values().removeIf(connection -> {
            if (connection.isTimedOut(timeout)) {
                connection.close();
                return true;
            }
            return false;
        });
    }

}
