package com.go2super.packet.boot;

import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseRoleInfo extends Packet {

    public static final int TYPE = 1007;

    private int gMapId;
    private int gId;
    private int consortiaId;
    private int propsPack;
    private int propsCorpPack;
    private char consortiaJob;
    private char consortiaUnionLevel;
    private char consortiaShopLevel;
    private char gameServerId;
    private int card1;
    private int cardCredit;
    private int card2;
    private int card3;
    private int cardUnion;
    private int chargeFlag;
    private int addPackMoney;
    private int lotteryCredit;
    private int shipSpeedCredit;
    private int lotteryStatus;
    private int consortiaThrow;
    private int consortiaUnion;
    private int consortiaShop;
    private SmartString name = SmartString.of(32);
    private int ectypeNum;
    private int badge;
    private int honor;
    private UnsignedInteger serverTime = UnsignedInteger.of(1000);
    private int tollGate;
    private short year;
    private char month;
    private char day;
    private int noviceGuide;
    private UnsignedInteger warScore = UnsignedInteger.of(1000);

}
