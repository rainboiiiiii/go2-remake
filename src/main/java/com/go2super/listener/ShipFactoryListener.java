package com.go2super.listener;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.FactoryShip;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.obj.game.CreateShipInfo;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.shipmodel.*;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.exception.BadGuidException;

import java.util.ArrayList;
import java.util.List;

public class ShipFactoryListener implements PacketListener {

    public static int MAX_SHIPS = 2000000;

    @PacketProcessor
    public void onCreate(RequestCreateShipPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null || packet.getNum() <= 0)
            return;

        int currentShips = user.totalShips();

        if(currentShips + packet.getNum() >= MAX_SHIPS)
            return;

        ShipModel shipModel = PacketService.getShipModel(packet.getShipModelId());

        if(shipModel.getGuid() != -1)
            if(shipModel.getGuid() != user.getGuid() || shipModel.isDeleted())
                return;

        UserShips ships = user.getShips();

        int gas = shipModel.getHe3() * packet.getNum();
        int metal = shipModel.getMetal() * packet.getNum();
        int gold = shipModel.getGold() * packet.getNum();

        int buildTime = shipModel.getBuildTime();
        int needTime = buildTime * packet.getNum();

        if(!ships.fabricate(packet.getShipModelId(), packet.getNum(), buildTime))
            return;

        ResponseCreateShipPacket response = new ResponseCreateShipPacket();

        response.setGas(gas);
        response.setMetal(metal);
        response.setMoney(gold);
        response.setNeedTime(needTime);
        response.setNum(packet.getNum());
        response.setShipModelId(packet.getShipModelId());

        user.save();
        packet.getSmartServer().send(response);

        System.out.println(buildTime + ", " + metal + ", " + gas + ", " + gold);

    }

    @PacketProcessor
    public void onDesignShip(RequestCreateShipModelPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null || packet.getPartNum() <= 0 || packet.getPartNum() > 50)
            return;

        List<ShipModel> models = PacketService.getShipModelRepository().findAllByGuidAndDeleted(packet.getGuid(), false);

        if(models.size() >= 29)
            return;

        List<Integer> parts = new ArrayList<>();

        for(int i = 0; i < packet.getPartNum(); i++)
            parts.add(packet.getParts().get(i));

        ShipModel model = ShipModel.builder()
                .shipModelId(PacketService.getInstance().getNextShipModelId())
                .bodyId(packet.getBodyId())
                .deleted(false)
                .guid(packet.getGuid())
                .name(packet.getShipName().noSpaces())
                .parts(parts)
                .build();

        ResponseCreateShipModelPacket response = new ResponseCreateShipModelPacket();

        response.setShipModelId(model.getShipModelId());
        response.setBodyId((short) model.getBodyId());
        response.getShipName().setValue(model.getName());
        response.setParts(packet.getParts());
        response.setPartNum(packet.getPartNum());
        response.setNeedMoney(0);

        PacketService.getShipModelRepository().save(model);
        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onCancelShip(RequestCancelShipPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        if(packet.getIndexId() < 0 || packet.getIndexId() > 4)
            return;

        UserShips ships = user.getShips();
        List<FactoryShip> factory = ships.getFactory();

        if(factory.size() <= packet.getIndexId())
            return;

        FactoryShip factoryShip = factory.get(packet.getIndexId());
        factory.remove(factoryShip);

        user.save();

        ResponseCancelShipPacket response = new ResponseCancelShipPacket();

        response.setIndexId(packet.getIndexId());
        response.setNum(factoryShip.getNum());
        response.setStatus(1);

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onDelete(RequestDeleteShipModelPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        ShipModel model = PacketService.getShipModel(packet.getShipModelId());

        if(model == null || model.getGuid() != packet.getGuid())
            return;

        model.setDeleted(true);
        PacketService.getShipModelRepository().save(model);

    }

    @PacketProcessor
    public void onFactory(RequestCreateShipInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if (user == null)
            return;

        UserShips ships = user.getShips();
        ResponseCreateShipInfoPacket response = new ResponseCreateShipInfoPacket();

        response.setMaxCreateShipNum(2000000 - user.totalShips());
        response.setIncShipPercent((short) 0);
        response.setDataLen((short) ships.getFactory().size());

        List<CreateShipInfo> factory = user.getShips().getFactoryAsBuffer();
        CreateShipInfo reference = new CreateShipInfo();

        for(FactoryShip ship : ships.getFactory())
            factory.add(ship.packet());

        while(factory.size() < 10)
            factory.add(reference.trash());

        response.setCreateShipList(factory);
        packet.getSmartServer().send(response);

    }

}
