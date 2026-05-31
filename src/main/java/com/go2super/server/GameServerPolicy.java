package com.go2super.server;

import com.go2super.buffer.Go2Buffer;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.packet.PacketRouter;
import com.go2super.service.PacketService;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.SocketUtil;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.mina.core.buffer.IoBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Data
public class GameServerPolicy extends SmartServer implements Runnable {

    private PacketRouter packetRouter;
    private int currentPacketId = 0;

    private IoBuffer cache;
    private Go2Buffer buffer;

    private long last = 0;

    private Thread thread;

    @SneakyThrows
    public GameServerPolicy(Socket socket, PacketRouter packetRouter, GameServer gameServer) {
        super(gameServer.getPort());

        setSocket(socket);
        setInputStream(new DataInputStream(socket.getInputStream()));
        setOutputStream(new DataOutputStream(socket.getOutputStream()));

        this.packetRouter = packetRouter;

    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    @SneakyThrows
    @Override
    public void run() {

        try {

            Socket socket = getSocket();
            last = new Date().getTime();

            BotLogger.thread("User policy thread opened!");

            while(!socket.isClosed() && socket.isConnected()) {
                if(tick())
                    break;
            }

            close();
            return;

        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

    }

    @SneakyThrows
    public boolean tick() {

        if(getInputStream().available() == 0) {

            long now = new Date().getTime();
            long diff = now - last;

            if(diff >= PacketService.getInstance().getTimeout() * 1000 || getSocket().isClosed() || !getSocket().isConnected()) {

                close();
                return false;

            }

            Thread.sleep(PacketService.getInstance().getInterval());
            return false;

        }

        byte[] data = new byte[23];
        int bytesRead = getInputStream().read(data);

        if(bytesRead == -1)
            return false;

        if(getCurrentPacketId() == 0 && bytesRead == 23) {

            IoBuffer buffer = IoBuffer.allocate(90);
            buffer.putString("<cross-domain-policy><allow-access-from domain='*' to-ports='*' /> </cross-domain-policy>", StandardCharsets.UTF_8.newEncoder());
            getOutputStream().write(buffer.array());

            close();
            return true;

        }

        return false;

    }

    @Override
    public void refresh() {

        last = new Date().getTime();
        return;

    }

    @Override
    @SneakyThrows
    public void close() {

        if(!getSocket().isClosed())
            getSocket().close();

        BotLogger.thread("User policy thread closed!");
        Thread.currentThread().stop();
        return;

    }

}
