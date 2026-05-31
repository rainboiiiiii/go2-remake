package com.go2super.server;

import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.packet.PacketRouter;
import com.go2super.service.PacketService;
import com.go2super.socket.util.SocketUtil;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.mina.core.buffer.IoBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.util.Date;

@Data
public class GameServerReceiver extends SmartServer implements Runnable {

    private final PacketRouter packetRouter;
    private int currentPacketId = 0;

    private long last = 0;

    @SneakyThrows
    public GameServerReceiver(Socket socket, PacketRouter packetRouter, GameServer gameServer) {
        super(gameServer.getPort());

        setSocket(socket);
        setRemoteAddress(socket.getInetAddress());
        setInputStream(new DataInputStream(socket.getInputStream()));
        setOutputStream(new DataOutputStream(socket.getOutputStream()));

        this.packetRouter = packetRouter;

    }

    @SneakyThrows
    @Override
    public void run() {

        try {

            Socket socket = getSocket();
            BotLogger.thread("User thread opened!");

            refresh();

            while(!socket.isClosed() && socket.isConnected()) {
                byte[] data = new byte[2 * 1024];
                tick(data);
            }

        } catch(Exception e) {
            return;
        }

    }

    @SneakyThrows
    public void tick(byte[] data) {

        if(getInputStream().available() == 0) {

            long now = new Date().getTime();
            long diff = now - last;

            if(diff >= PacketService.getInstance().getTimeout() * 1000 || getSocket().isClosed() || !getSocket().isConnected()) {

                close();
                return;

            }

            Thread.sleep(PacketService.getInstance().getInterval());
            return;

        }

        int bytesRead = getInputStream().read(data);

        if(bytesRead == -1)
            return;

        setCurrentPacketId(getCurrentPacketId() + 1);
        IoBuffer realBuffer = PacketFraming.toIoBuffer(data, bytesRead);

        try {
            PacketFraming.processBuffer(realBuffer, packetRouter, getSocket(), this, true);
        } catch (BufferUnderflowException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void refresh() {

        last = new Date().getTime();
        lastActivity = last;
        return;

    }

    @Override
    @SneakyThrows
    public void close() {

        if(!getSocket().isClosed())
            getSocket().close();

        BotLogger.thread("User thread closed!");

        try {
            Thread.currentThread().stop();
        } catch(ThreadDeath death) {}

        return;

    }

}
