package com.go2super.packet.science;

import com.go2super.obj.game.TechUpgradeInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseTechUpgradeInfoPacket extends Packet {

    public static final int TYPE = 1216;

    private short incTechPercent;

    private int dataLen;
    private List<TechUpgradeInfo> techUpgradeInfoList = new ArrayList<>();

}
