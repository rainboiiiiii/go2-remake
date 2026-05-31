package com.go2super.packet.instance;

import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseEctypeStatePacket extends Packet {

    public static final int TYPE = 1135;

    private int ectypeId;

    private UnsignedChar gateId;
    private char state;

}