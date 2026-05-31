package com.go2super.packet.corps;

import com.go2super.packet.Packet;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestConsortiaInfoPacket extends Packet {

    public static final int TYPE = 1657;

    private int seqId;
    private int guid;

    private int pageId;

}
