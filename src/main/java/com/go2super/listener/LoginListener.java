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

import java.util.Optional;

public class LoginListener implements PacketListener {

    @SneakyThrows
    @PacketProcessor
    public void onLoginTol(PlayerLoginTolPacket packet) {

        LoginService loginService = LoginService.getInstance();

        Optional<LoggedSessionUser> sessionUser = loginService.getSession(packet.getUserId());

        if(!sessionUser.isPresent()) {
            BotLogger.login("Login rejected (502): no session for userId=" + packet.getUserId());
            cancel(packet);
            return;
        }

        LoggedSessionUser session = sessionUser.get();
        String sessionKey = session.getSessionKey();

        if(!packet.getSessionKey().matchesSessionKey(sessionKey, 25)) {
            BotLogger.login("Login rejected (502): session key mismatch for userId=" + packet.getUserId());
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
            BotLogger.login("Login rejected (503): no session for userId=" + packet.getUserId());
            cancel(packet);
            return;
        }

        LoggedSessionUser session = sessionUser.get();

        String sessionKey = session.getSessionKey();

        if(!packet.getSessionKey().matchesSessionKey(sessionKey, 25)) {
            BotLogger.login("Login rejected (503): session key mismatch for userId=" + packet.getUserId());
            cancel(packet);
            return;
        }

        loginService.disconnectGame(session);

        LoggedGameUser gameUser = loginService.login(session, packet);

        Go2Buffer resgo2 = new Go2Buffer(32);
        resgo2.addShort(0); // SIZE (patched below)
        resgo2.addShort(505); // TYPE
        resgo2.addByte((byte) 0); // UNKNW

        resgo2.addShort(0); // Necessarily Trash
        resgo2.addByte((byte) 0); // Necessarily Trash

        resgo2.addInt(gameUser.getGuid()); // guid
        resgo2.addInt(1); // guide

        resgo2.getBuffer().putShort(0, (short) resgo2.getCalculatedSize());

        packet.getSmartServer().send(resgo2);
        BotLogger.login("User entered game (guid=" + gameUser.getGuid() + ", userId=" + packet.getUserId() + ", transport=websocket-tog)");

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
