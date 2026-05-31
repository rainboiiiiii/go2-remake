package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.Prop;
import com.go2super.obj.type.PropAction;
import com.go2super.obj.utility.PropConsumption;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.props.RequestUsePropsPacket;
import com.go2super.service.PacketService;

public class PropListener implements PacketListener {

    @PacketProcessor
    public void useProp(RequestUsePropsPacket packet) {

        BotLogger.dev("PropId :: [" + packet.getPropsId() + "]");

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());
        PropAction consumption = PropAction.getAction(packet.getPropsId());

        if(packet.getNum() <= 0)
            return;

        Prop prop = user.getInventory().getProp(packet.getPropsId(), 0);
        boolean lock = packet.getLockFlag() == 1;

        if(consumption == null || prop == null)
            return;

        if(!user.getInventory().removeProp(prop.getPropId(), packet.getNum(), 0, lock))
            return;

        BotLogger.dev("PropId :: [" + prop.getPropId() + "] Consumer :: [" + consumption.name() + "]");

        PropConsumption action = consumption.getAction();
        action.consume(prop, packet.getNum(),  lock, packet, user);

    }

}
