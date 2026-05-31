package com.go2super.packet.ship;

import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class RequestCreateShipTeamPacket extends Packet {

    public static final int TYPE = 1316;

    private int seqId;
    private int guid;

    private SmartString name = SmartString.of(32);
    private ShipTeamBody teamBody = new ShipTeamBody();

    private int commanderId;

    private char target;
    private char targetInterval;

}
