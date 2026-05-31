package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.logger.BotLogger;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.chat.ChatMessagePacket;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.exception.BadGuidException;
import java.util.NoSuchElementException;

public class ChatListener implements PacketListener {
    
    public static final int CHANNEL_GLOBAL = 0;
    public static final int CHANNEL_PLANET = 1;
    public static final int CHANNEL_CORPS = 2;
    public static final int CHANNEL_PRIVATE = 3;
    
    @PacketProcessor
    public void onChat(ChatMessagePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        BotLogger.dev(packet.getGuid() + " :: [CHAT] " + user.getUsername() + " :: " + packet.getBuffer().getValue());

        ChatMessagePacket response = new ChatMessagePacket();

        response.setSeqId(packet.getSeqId() + 1);
        response.setSrcUserId(packet.getSrcUserId());
        response.setObjUserId(packet.getObjUserId());
        response.setGuid(packet.getGuid());
        response.setObjGuid(packet.getObjGuid());
        response.setChannelType(packet.getChannelType());
        response.setSpecialType(packet.getSpecialType());
        response.setPropsId(packet.getPropsId());
        response.setName(WideString.of(user.getUsername(), 32));
        response.setToName(packet.getToName());
        response.setBuffer(packet.getBuffer());
        
        switch(packet.getChannelType()) {
          case CHANNEL_GLOBAL:
            LoginService.getInstance()
                .getGameUsers()
                .stream()
                .forEach(u -> u.getSmartServer().send(response));
            break;
          case CHANNEL_PLANET:
            try {
              LoggedGameUser loggedUser = LoginService.getInstance().getGame( packet.getGuid() ).get();
              LoginService.getInstance()
                .getGameUsers()
                .stream()
                .filter(u -> u.getViewing() == loggedUser.getViewing() )
                .forEach(u -> u.getSmartServer().send(response));
            } catch( NoSuchElementException ex ) {}
            break;
          case CHANNEL_CORPS:
            LoginService.getInstance()
                .getGameUsers()
                .stream()
                .filter(u -> u.getUpdatedUser().getConsortiaId() == user.getConsortiaId())
                .forEach(u -> u.getSmartServer().send(response));
            break;
          case CHANNEL_PRIVATE:
            LoginService.getInstance()
                .getGameUsers()
                .stream()
                .filter(u -> u.getUpdatedUser().getGuid() == packet.getObjGuid())
                .limit(1) //should only be one anyways
                .forEach(u -> {
                  packet.getSmartServer().send(response);
                  u.getSmartServer().send(response);
                });
            break;
        }

    }

}
