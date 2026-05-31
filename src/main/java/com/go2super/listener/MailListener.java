package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.instance.RequestEctypePacket;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.exception.BadGuidException;

public class MailListener implements PacketListener {

    @PacketProcessor
    public void onSendEmail(RequestEctypePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        System.out.println(packet);

    }

}
