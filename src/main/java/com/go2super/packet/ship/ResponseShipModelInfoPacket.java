package com.go2super.packet.ship;

import com.go2super.obj.game.ShipModelInfo;
import com.go2super.obj.utility.Trash;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class ResponseShipModelInfoPacket extends Packet {

    public static final int TYPE = 1303;

    private UnsignedShort dataLen; // 7
    @Trash(length = 7) private List<ShipModelInfo> shipModelInfoList = new ArrayList<>();

}
