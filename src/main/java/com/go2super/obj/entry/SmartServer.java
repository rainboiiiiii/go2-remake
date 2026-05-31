package com.go2super.obj.entry;

import com.go2super.buffer.Go2Buffer;
import com.go2super.logger.BotLogger;
import com.go2super.packet.Packet;
import lombok.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

@Data
public abstract class SmartServer {

    protected String connectionId = UUID.randomUUID().toString();
    protected InetAddress remoteAddress;
    protected long lastActivity = System.currentTimeMillis();

    private Socket socket;
    private int port;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public SmartServer(int port) {
        this.port = port;
    }

    public void send(Packet packet) {
        BotLogger.packet("↗ SEND " + packet.getType() + " (" + packet.getClass().getSimpleName() + ")");
        send(packet.unmap());
    }

    public void send(Go2Buffer buffer) {
        try {
            writeBuffer(buffer);
            refresh();
        } catch (Exception e) {
            close();
        }
    }

    protected void writeBuffer(Go2Buffer buffer) throws Exception {
        outputStream.write(buffer.getBuffer().array());
        outputStream.flush();
    }

    public InetAddress getRemoteAddress() {
        if (remoteAddress != null) {
            return remoteAddress;
        }
        if (socket != null) {
            return socket.getInetAddress();
        }
        return null;
    }

    public abstract void refresh();

    public abstract void close();

}
