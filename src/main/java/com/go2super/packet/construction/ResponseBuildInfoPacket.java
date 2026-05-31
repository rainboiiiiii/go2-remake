package com.go2super.packet.construction;

import com.go2super.obj.game.BuildInfo;
import com.go2super.obj.game.EctypeInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseBuildInfoPacket extends Packet {

    public static final int TYPE = 1203;

    private int galaxyMapId;
    private int galaxyId;

    private short consortiaLeader;

    private char viewFlag; // 1 == PUEDE VER LA BASE ESPACIAL || 0 === NO PUEDE VER LA BASE ESPACIAL
    private char starType; // Estilo del terreno del planeta (0, 1, 2)

    private int dataLen;
    private List<BuildInfo> buildInfoList = new ArrayList<>();

}
