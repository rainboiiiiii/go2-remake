package com.go2super.server.websocket;

import com.go2super.buffer.Go2Buffer;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import lombok.Getter;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class WebSocketSmartServer extends SmartServer {

    @Getter
    private final WebSocketSession session;

    public WebSocketSmartServer(WebSocketSession session) {
        super(0);
        this.session = session;
        setConnectionId(session.getId());
        setRemoteAddress(resolveRemoteAddress(session));
    }

    @Override
    protected void writeBuffer(Go2Buffer buffer) throws Exception {
        if (!session.isOpen()) {
            throw new IllegalStateException("WebSocket session is closed");
        }

        int length = buffer.getCalculatedSize() > 0 ? buffer.getCalculatedSize() : buffer.getBuffer().remaining();
        byte[] payload = new byte[length];
        buffer.getBuffer().mark();
        buffer.getBuffer().get(payload, 0, length);
        buffer.getBuffer().reset();

        synchronized (session) {
            session.sendMessage(new BinaryMessage(payload));
        }
    }

    @Override
    public void refresh() {
        lastActivity = System.currentTimeMillis();
    }

    @Override
    public void close() {
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            BotLogger.log("Failed to close WebSocket session: " + e.getMessage());
        }
    }

    public boolean isTimedOut(long timeoutSeconds) {
        return System.currentTimeMillis() - lastActivity >= timeoutSeconds * 1000L;
    }

    private static InetAddress resolveRemoteAddress(WebSocketSession session) {
        if (session.getRemoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
            if (socketAddress.getAddress() != null) {
                return socketAddress.getAddress();
            }
        }
        return null;
    }

}
