package com.go2super.packet.ship;

import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.game.ShortArray;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseCommanderInfoArrangePacket extends Packet {

    public static final int TYPE = 1516;

    private int dataLen;
    private IntegerArray data = new IntegerArray(60);

}
