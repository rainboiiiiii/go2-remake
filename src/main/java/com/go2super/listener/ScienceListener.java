package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.obj.game.TechUpgradeInfo;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.props.RequestTimeQueuePacket;
import com.go2super.packet.science.RequestTechUpgradeInfoPacket;
import com.go2super.packet.science.ResponseTechUpgradeInfoPacket;
import com.go2super.service.PacketService;

import java.util.List;

public class ScienceListener implements PacketListener {

    @PacketProcessor
    public void onTimeQueue(RequestTimeQueuePacket packet) {

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        packet.getSmartServer().send(user.getQueuesAsPacket());

    }

    @PacketProcessor
    public void onTechUpgradeInfo(RequestTechUpgradeInfoPacket packet) {

        ResponseTechUpgradeInfoPacket responseTechUpgradeInfoPacket = new ResponseTechUpgradeInfoPacket();
        List<TechUpgradeInfo> techUpgradeInfo = responseTechUpgradeInfoPacket.getTechUpgradeInfoList();

        responseTechUpgradeInfoPacket.setIncTechPercent((short) 66);
        responseTechUpgradeInfoPacket.setDataLen(0);

        techUpgradeInfo(techUpgradeInfo);

        packet.getSmartServer().send(responseTechUpgradeInfoPacket);

    }

    public void techUpgradeInfo(List<TechUpgradeInfo> techUpgradeInfo) {

        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1, 24227, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1710747216, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(1643976, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(35045152, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(1015837328, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-2, -1, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(190589600, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(35045152, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(1015837328, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(190745472, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1710743504, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(190745472, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1710821344, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-627261515, 9996, 0));

    }

}
