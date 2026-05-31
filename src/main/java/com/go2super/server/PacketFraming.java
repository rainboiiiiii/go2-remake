package com.go2super.server;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.entry.SmartServer;
import com.go2super.packet.PacketRouter;
import lombok.SneakyThrows;
import org.apache.mina.core.buffer.IoBuffer;

import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;

public final class PacketFraming {

    private PacketFraming() {
    }

    @SneakyThrows
    public static void processBuffer(
            IoBuffer realBuffer,
            PacketRouter packetRouter,
            Socket socket,
            SmartServer smartServer,
            boolean gameSlicePadding
    ) {
        IoBuffer cache = IoBuffer.allocate(realBuffer.remaining());
        cache.order(ByteOrder.LITTLE_ENDIAN);
        cache.put(realBuffer.buf());
        cache.position(0);

        Go2Buffer base = new Go2Buffer(cache, false);
        int fetched = 0;

        while (fetched < base.getBuffer().limit()) {
            int size = base.getShort();

            if (size <= 0) {
                break;
            }

            int sliceLength = gameSlicePadding ? size + 10 : size;
            Go2Buffer current = new Go2Buffer(realBuffer.getSlice(fetched, sliceLength), false);
            fetched += size;

            int packetSize = current.getMsgSize();
            int packetType = current.getMsgType();

            base.getBuffer().position(fetched);

            if (packetType <= 0 || packetSize <= 0) {
                continue;
            }

            try {
                smartServer.refresh();
                packetRouter.playPacket(packetSize, packetType, current, socket, smartServer);
            } catch (BufferUnderflowException e) {
                throw e;
            }
        }
    }

    public static IoBuffer toIoBuffer(byte[] data, int bytesRead) {
        IoBuffer realBuffer = IoBuffer.allocate(0);
        realBuffer.setAutoExpand(true);
        realBuffer.order(ByteOrder.LITTLE_ENDIAN);
        realBuffer.put(data, 0, bytesRead);
        realBuffer.flip();
        return realBuffer;
    }

}
