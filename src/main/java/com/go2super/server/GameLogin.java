package com.go2super.server;

import com.go2super.buffer.Go2Buffer;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.packet.PacketRouter;
import com.go2super.socket.util.SocketUtil;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.mina.core.buffer.IoBuffer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Data
public class GameLogin extends SmartServer implements Runnable {

    public static final Set<String> waiting = new HashSet<String>();
    public static final Map<String, Integer> logged = new HashMap<String, Integer>();

    private ServerSocket serverSocket;
    private final PacketRouter packetRouter;

    private long last = 0;

    @SneakyThrows
    public GameLogin(int port, PacketRouter packetRouter) {
        super(port);

        serverSocket = new ServerSocket(port);
        this.packetRouter = packetRouter;

    }

    @SneakyThrows
    @Override
    public void run() {

        if (getServerSocket() != null) {
            while(true) {

                Socket socket = serverSocket.accept();

                setInputStream(new DataInputStream(socket.getInputStream()));
                setOutputStream(new DataOutputStream(socket.getOutputStream()));

                byte[] data = new byte[16 * 1024];
                int bytesRead = getInputStream().read(data);

                IoBuffer realBuffer = IoBuffer.allocate(0);
                realBuffer.setAutoExpand(true);
                realBuffer.order(ByteOrder.LITTLE_ENDIAN);
                realBuffer.put(data);

                IoBuffer cache = IoBuffer.allocate(realBuffer.array().length);
                cache.put(realBuffer.array(), 0, realBuffer.array().length);
                cache.position(0);

                if(new String(cache.array()).contains("<policy-file-request/>")) {

                    IoBuffer buffer = IoBuffer.allocate(90);
                    buffer.putString("<cross-domain-policy><allow-access-from domain='*' to-ports='*' /> </cross-domain-policy>", StandardCharsets.UTF_8.newEncoder());
                    getOutputStream().write(buffer.array());
                    continue;

                }

                Go2Buffer base = new Go2Buffer(cache, false);
                int fetched = 0;

                while(fetched < base.getBuffer().limit()) {

                    int size = base.getShort();

                    if(size <= 0) {
                        fetched += 2;
                        continue;
                    }

                    Go2Buffer current = new Go2Buffer(realBuffer.getSlice(fetched, size), false);
                    fetched += size;

                    int packetSize = current.getMsgSize();
                    int packetType = current.getMsgType();

                    base.getBuffer().position(fetched);
                    // BotLogger.dev("LOGIN PKT SIZE = " + packetSize + ", PKT TYPE " + packetType + ", CURRENT SIZE = " + current.getBuffer().limit());

                    try {

                        waiting.add(SocketUtil.getRemoteIP(socket));

                        PacketRouter.getInstance().playPacket(packetSize, packetType, current, socket, this);
                        socket.close();

                    } catch(BufferUnderflowException e) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void refresh() {

        last = new Date().getTime();
        return;

    }

    @Override
    public void close() {

        BotLogger.thread("User thread closed!");
        Thread.currentThread().stop();
        return;

    }
}

