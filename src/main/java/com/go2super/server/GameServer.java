package com.go2super.server;

import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.packet.PacketRouter;
import com.go2super.socket.util.SocketUtil;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.mina.core.buffer.IoBuffer;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.*;

@Data
public class GameServer extends SmartServer implements Runnable {

    private Map<GameServerReceiver, Thread> clientsThreads = new HashMap<>();
    private List<GameServerReceiver> clients = new ArrayList<>();

    private ServerSocket serverSocket;
    private final PacketRouter packetRouter;

    private long last = 0;

    @SneakyThrows
    public GameServer(int port, PacketRouter packetRouter) {
        super(port);

        serverSocket = new ServerSocket(port);
        this.packetRouter = packetRouter;

    }

    @SneakyThrows
    @Override
    public void run() {

        IoBuffer cache = IoBuffer.allocate(0);
        cache.order(ByteOrder.LITTLE_ENDIAN);

        if(getServerSocket() != null) {
            while(true) {

                Socket socket = serverSocket.accept();
                String ip = SocketUtil.getRemoteIP(socket);

                if(GameLogin.waiting.contains(ip)) {

                    GameServerPolicy receiver = new GameServerPolicy(socket, packetRouter, this);

                    Thread thread = new Thread(receiver);
                    receiver.setThread(thread);
                    thread.start();

                    GameLogin.waiting.remove(ip);
                    continue;

                }

                GameServerReceiver receiver = new GameServerReceiver(socket, packetRouter, this);

                Thread thread = new Thread(receiver);
                thread.start();

                clients.add(receiver);
                clientsThreads.put(receiver, thread);

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

