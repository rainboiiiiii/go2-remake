package com.go2super.packet.commander;

import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCommanderEditShipTeamPacket extends Packet {

    public static final int TYPE = 1517;

    private int seqId;
    private int guid;
    private int shipTeamId;

    private ShipTeamBody teamBody = new ShipTeamBody();

    private char target;
    private char targetInterval;

    private char kind;

}
