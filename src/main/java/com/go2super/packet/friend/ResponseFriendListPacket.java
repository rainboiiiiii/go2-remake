package com.go2super.packet.friend;

import com.go2super.obj.game.FriendInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseFriendListPacket extends Packet {

    public static final int TYPE = 1606;

    private char dataLen;
    private char kind;

    private short friendCount;
    private List<FriendInfo> friendInfos = new ArrayList<>();

}