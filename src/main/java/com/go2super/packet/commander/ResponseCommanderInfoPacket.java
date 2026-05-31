package com.go2super.packet.commander;

import com.go2super.obj.game.*;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.Trash;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseCommanderInfoPacket extends Packet {

    public static final int TYPE = 1507;

    private int commanderId;
    private int shipTeamId;
    private int restTime;
    private int exp;

    private short aim;
    private short blench;
    private short priority;
    private short electron;
    private short skill;
    private short cardLevel;

    private char level;
    private char cardType;
    private char state;
    private char showType;
    private SmartString commanderZJ = SmartString.of("CCCCCCCC", 8);

    @Trash(length = 9) private List<ShipTeamNum> teamBody = new ArrayList<>();

    private char target;
    private char targetInterval;
    private char reserve;
    private char allStatusLen;

    @Trash(length = 60) private List<CommanderInfo> allStatus = new ArrayList<>();

    private ShortArray stone = new ShortArray(12);

    private int stoneHole;
    private char aimPer = (char) 32;
    private char blenchPer = (char) 32;
    private char priorityPer = (char) 32;
    private char electronPer = (char) 32;

    private IntegerArray cmosExp = new IntegerArray(5);
    private ShortArray cmos = new ShortArray(5);

}
