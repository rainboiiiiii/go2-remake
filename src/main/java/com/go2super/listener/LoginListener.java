package com.go2super.listener;

import com.go2super.buffer.Go2Buffer;
import com.go2super.database.entity.User;
import com.go2super.logger.BotLogger;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.model.LoggedSessionUser;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.login.PlayerLoginServerValidatePacket;
import com.go2super.packet.login.PlayerLoginTogPacket;
import com.go2super.packet.login.PlayerLoginTolPacket;
import com.go2super.server.websocket.WebSocketSmartServer;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import lombok.SneakyThrows;
import org.apache.mina.core.buffer.IoBuffer;

import java.nio.ByteOrder;
import java.util.Optional;

public class LoginListener implements PacketListener {

    @SneakyThrows
    @PacketProcessor
    public void onLoginTol(PlayerLoginTolPacket packet) {

        LoginService loginService = LoginService.getInstance();

        Optional<LoggedSessionUser> sessionUser = loginService.getSession(packet.getUserId());

        if(!sessionUser.isPresent()) {
            cancel(packet);
            return;
        }

        LoggedSessionUser session = sessionUser.get();
        String sessionKey = session.getSessionKey();

        if(!packet.getSessionKey().shrink(25).equals(sessionKey)) {
            cancel(packet);
            return;
        }

        User user = session.getUser();

        if(user == null)
            return;

        loginService.disconnectGame(session);

        PlayerLoginServerValidatePacket response = new PlayerLoginServerValidatePacket();

        if (packet.getSmartServer() instanceof WebSocketSmartServer) {
            response.setPort(0);
            response.getIp().setValue("");
        } else {
            response.setPort(90);
            response.getIp().setValue(PacketService.getInstance().getServerIp());
        }

        response.setUserId(packet.getUserId());
        response.getSessionKey().setValue(sessionKey);

        packet.getSmartServer().send(response);
        BotLogger.login("User logged successfully (Name: " + user.getUsername() + ", Id: " + user.getGuid() + ", transport: " + (packet.getSmartServer() instanceof WebSocketSmartServer ? "websocket" : "tcp") + ")");

    }

    @PacketProcessor
    public void onLoginTog(PlayerLoginTogPacket packet) {

        LoginService loginService = LoginService.getInstance();

        Optional<LoggedSessionUser> sessionUser = loginService.getSession(packet.getUserId());

        if(!sessionUser.isPresent()) {
            cancel(packet);
            return;
        }

        LoggedSessionUser session = sessionUser.get();

        String sessionKey = session.getSessionKey();

        if(!packet.getSessionKey().shrink(25).equals(sessionKey)) {
            cancel(packet);
            return;
        }

        loginService.disconnectGame(session);

        LoggedGameUser gameUser = loginService.login(session, packet);

        IoBuffer response = IoBuffer.allocate(16);
        response.order(ByteOrder.LITTLE_ENDIAN);

        Go2Buffer resgo2 = new Go2Buffer(response, false);
        resgo2.addShort(response.limit()); // SIZE
        resgo2.addShort(505); // TYPE
        resgo2.getBuffer().put((byte) 0); // UNKNW

        resgo2.addShort(0); // Necessarily Trash
        resgo2.addByte((byte) 0); // Necessarily Trash

        resgo2.addInt(gameUser.getGuid()); // guid
        resgo2.addInt(1); // guide

        packet.getSmartServer().send(resgo2);

    }

    @SneakyThrows
    public void cancel(PlayerLoginTolPacket packet) {
        packet.getSmartServer().close();
    }

    @SneakyThrows
    public void cancel(PlayerLoginTogPacket packet) {
        packet.getSmartServer().close();
    }

}
