package com.go2super.listener;

import com.go2super.database.entity.Match;
import com.go2super.database.entity.User;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.instance.RequestEctypePacket;
import com.go2super.packet.instance.ResponseEctypePassPacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.exception.BadGuidException;

import java.util.ArrayList;
import java.util.List;

public class InstanceListener implements PacketListener {

    @PacketProcessor
    public void onEctype(RequestEctypePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null || packet.getDataLen().getValue() <= 0)
            return;

        Match current = BattleService.getInstance().getCurrent(user);

        if(current != null)
            return;

        List<Integer> fleetIds = new ArrayList<>();

        for(int i = 0; i < packet.getDataLen().getValue(); i++)
            fleetIds.add(packet.getShips().getArray()[i]);

        MatchRunnable runnable = BattleService.getInstance().makeInstanceMatch(user, fleetIds, packet.getEctypeId());
        Match match = runnable.getMatch();

        if(runnable == null)
            return;

        ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

        response.setEctypeId(match.getMatchId());
        response.setGateId(UnsignedChar.of(2));
        response.setState((char) 0);

        packet.getSmartServer().send(response);
        //System.out.println(packet);

    }

}
