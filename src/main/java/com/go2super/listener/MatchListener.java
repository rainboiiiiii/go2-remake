package com.go2super.listener;

import com.go2super.database.entity.Match;
import com.go2super.database.entity.User;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.match.RequestMatchInfoPacket;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.exception.BadGuidException;

public class MatchListener implements PacketListener {

    @PacketProcessor
    public void onMatchInfo(RequestMatchInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Match current = BattleService.getInstance().getCurrent(user);

        if(current == null)
            return;

        System.out.println("CURRENT = " + current);
        System.out.println("PACKET = " + packet);

    }

}
