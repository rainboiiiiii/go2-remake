package com.go2super.packet.upgrade;

import com.go2super.obj.game.ShipBodyInfo;
import com.go2super.obj.game.ShortArray;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseShipBodyUpgradeInfoPacket extends Packet {

    public static final int TYPE = 1369;

    private int incUpgradePercent;
    private short bodyNum;
    private short partNum;

    private List<ShipBodyInfo> bodyId = new ArrayList<>();
    private List<ShipBodyInfo> partId = new ArrayList<>();

}
