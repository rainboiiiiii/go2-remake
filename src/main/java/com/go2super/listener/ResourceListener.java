package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.database.entity.sub.UserStorage;
import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.boot.RequestPlayerResourcePacket;
import com.go2super.packet.boot.ResponsePlayerResourcePacket;
import com.go2super.packet.construction.RequestGetStorageResourcePacket;
import com.go2super.packet.construction.ResponseGetStorageResourcePacket;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;

public class ResourceListener implements PacketListener {

    @PacketProcessor
    public void onPlayerResource(RequestPlayerResourcePacket packet) {

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());
        user.update();

        ResponsePlayerResourcePacket responsePlayerResourcePacket = new ResponsePlayerResourcePacket();

        responsePlayerResourcePacket.setUserGas(UnsignedInteger.of(user.getResources().getHe3()));
        responsePlayerResourcePacket.setUserMetal(UnsignedInteger.of(user.getResources().getMetal()));
        responsePlayerResourcePacket.setUserMoney(UnsignedInteger.of(user.getResources().getGold()));

        responsePlayerResourcePacket.setCredit(UnsignedInteger.of(user.getResources().getMallPoints()));
        responsePlayerResourcePacket.setLevel(user.getStats().getLevel());
        responsePlayerResourcePacket.setExp(user.getStats().getExp());
        responsePlayerResourcePacket.setCoins(user.getResources().getVouchers());
        responsePlayerResourcePacket.setOutGas(user.getStorage().getHe3Production());
        responsePlayerResourcePacket.setOutMetal(user.getStorage().getMetalProduction());
        responsePlayerResourcePacket.setOutMoney(user.getStorage().getGoldProduction());
        responsePlayerResourcePacket.setMaxSpValue(user.getStats().getMaxSp());
        responsePlayerResourcePacket.setSpValue(user.getStats().getSp());
        responsePlayerResourcePacket.setMoneyBuyNum(0);
        responsePlayerResourcePacket.setDefyEctypeNum(0);
        responsePlayerResourcePacket.setMatchCount(0);
        responsePlayerResourcePacket.setTollGate(0);
        responsePlayerResourcePacket.setReserve(0);

        UserService.getInstance().updateStats(user);
        packet.getSmartServer().send(responsePlayerResourcePacket);

    }

    @PacketProcessor
    public void onGetResources(RequestGetStorageResourcePacket packet) {

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());
        user.update();

        UserStorage storage = user.getStorage();
        UserResources resources = user.getResources();

        int totalGold = storage.getGold();
        int totalHe3 = storage.getHe3();
        int totalMetal = storage.getMetal();

        resources.addGold(totalGold);
        resources.addHe3(totalHe3);
        resources.addMetal(totalMetal);

        storage.reset();

        ResponseGetStorageResourcePacket response = new ResponseGetStorageResourcePacket();

        response.setMoney(totalGold);
        response.setGas(totalHe3);
        response.setMetal(totalMetal);

        packet.getSmartServer().send(response);
        user.save();

    }

}
