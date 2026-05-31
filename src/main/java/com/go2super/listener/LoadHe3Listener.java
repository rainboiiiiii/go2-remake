package com.go2super.listener;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.game.ShipHe3Info;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.loadhe3.*;
import com.go2super.packet.ship.*;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.exception.BadGuidException;

import java.util.ArrayList;
import java.util.List;

public class LoadHe3Listener implements PacketListener {

    private int MAX_RESOURCE = 900000000;

    @PacketProcessor
    public void onLoadShipTeamAll(RequestLoadShipTeamAllPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        List<Fleet> fleets = user.getFleets();

        ResponseLoadShipTeamAllPacket response = new ResponseLoadShipTeamAllPacket();

        response.setSeqId(packet.getSeqId() + 1);
        response.setGuid(packet.getGuid());

        response.setDataLen(fleets.size());

        for(Fleet fleet : fleets) {

            ShipHe3Info ship = new ShipHe3Info();

            ship.getShipName().value(fleet.getName());
            ship.setGas(fleet.getHe3());
            ship.setShipSpace(fleet.getMaxHe3());
            ship.setShipNum(fleet.ships());
            ship.setShipTeamId(fleet.getShipTeamId());
            ship.setSupply(10);

            response.getShips().add(ship);

        }

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onLoadShipTeam(RequestLoadShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        if(packet.getGas().getValue() <= 0 || packet.getGas().getValue() > MAX_RESOURCE)
            return;

        Fleet fleet = PacketService.getFleetRepository().findByShipTeamId(packet.getShipTeamId());

        if(fleet == null || fleet.getGuid() != user.getGuid())
            return;

        int value = packet.getGas().getValue();
        int newValue = fleet.getHe3() + value;

        if(newValue < 0 || newValue > fleet.getMaxHe3())
            return;

        if(user.getResources().getHe3() - value < 0)
            return;

        user.getResources().setHe3(user.getResources().getHe3() - value);

        fleet.setHe3(newValue);
        fleet.save();
        user.save(); // todo changes

        ResponseLoadShipTeamPacket response = new ResponseLoadShipTeamPacket();

        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());
        response.setShipTeamId(packet.getShipTeamId());
        response.setGas(packet.getGas());

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onUnloadShipTeam(RequestUnloadShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        if(packet.getGas().getValue() <= 0 || packet.getGas().getValue() > MAX_RESOURCE)
            return;

        Fleet fleet = PacketService.getFleetRepository().findByShipTeamId(packet.getShipTeamId());

        if(fleet == null || fleet.getGuid() != user.getGuid())
            return;

        int value = packet.getGas().getValue();
        int newValue = fleet.getHe3() - value;

        System.out.println(packet);

        if(newValue < 0 || newValue > fleet.getMaxHe3())
            return;

        user.getResources().addHe3(value);

        fleet.setHe3(newValue);
        fleet.save();
        user.save(); // todo changes

        ResponseUnloadShipTeamPacket response = new ResponseUnloadShipTeamPacket();

        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());
        response.setShipTeamId(packet.getShipTeamId());
        response.setGas(packet.getGas());

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onCreateFleet(RequestCreateShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Commander commander = user.getCommander(packet.getCommanderId());

        if(commander == null)
            return;

        if(PacketService.getFleetRepository().findByCommanderId(commander.getCommanderId()) != null)
            return;

        ShipTeamBody teamBody = packet.getTeamBody();
        UserShips ships = user.getShips();

        int total = 0;
        int body = -1;

        for(ShipTeamNum shipTeamNum : teamBody.getCells()) {

            if(shipTeamNum.getShipModelId() <= -1 || shipTeamNum.getNum() <= 0)
                continue;

            if(shipTeamNum.getNum() > 3000)
                return;

            if(!ships.removeShip(shipTeamNum.getShipModelId(), shipTeamNum.getNum()))
                return;

            ShipModel model = PacketService.getShipModel(shipTeamNum.getShipModelId());

            if(model == null)
                return;

            if(body < model.getBodyId())
                body = model.getBodyId();

            total += shipTeamNum.getNum();

        }

        if(total <= 0)
            return;

        Fleet fleet = Fleet.builder()
                .shipTeamId(PacketService.getInstance().getNextShipTeamId())
                .bodyId(body)
                .galaxyId(user.getPlanet().getPosition().galaxyId())
                .fleetBody(teamBody)
                .name(packet.getName().noSpaces())
                .commanderId(commander.getCommanderId())
                .guid(user.getGuid())
                .target(packet.getTarget())
                .targetInterval(packet.getTargetInterval())
                .posX(13)
                .posY(13)
                .build();

        user.save();
        PacketService.getFleetRepository().save(fleet);

        ResponseCreateShipTeamPacket response = new ResponseCreateShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());

        GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();

        fleetInfo.setShipTeamId(fleet.getShipTeamId());
        fleetInfo.setShipNum(fleet.ships());
        fleetInfo.setBodyId((short) body);
        fleetInfo.setReserve((short) 0);
        fleetInfo.setDirection((char) 0);

        fleetInfo.setPosX((char) fleet.getPosX());
        fleetInfo.setPosY((char) fleet.getPosY());
        fleetInfo.setOwner((char) 2);

        response.setGalaxyFleetInfo(fleetInfo);
        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onEditShipTeam(RequestEditShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null || packet.getShipTeamId() < 0)
            return;

        Commander commander = user.getCommander(packet.getCommanderId());
        Fleet ccFleet = commander.getFleet();

        if(commander == null || (ccFleet != null && ccFleet.getShipTeamId() != packet.getShipTeamId()))
            return;

        Fleet fleet = PacketService.getFleetRepository().findByShipTeamId(packet.getShipTeamId());

        if(fleet == null || fleet.getShipTeamId() != packet.getShipTeamId())
            return;

        ShipTeamBody oldTeamBody = fleet.getFleetBody();
        ShipTeamBody newTeamBody = packet.getTeamBody();

        UserShips ships = user.getShips();

        int total = 0;
        int body = -1;

        if(newTeamBody.getCells().size() != 9)
            return;

        for(ShipTeamNum oldNum : oldTeamBody.getCells())
            if(oldNum.getNum() != 0 && oldNum.getShipModelId() > -1)
                ships.addShip(oldNum.getShipModelId(), oldNum.getNum());

        for(ShipTeamNum newNum : newTeamBody.getCells()) {

            if(newNum.getShipModelId() <= -1 || newNum.getNum() <= 0)
                continue;

            if(newNum.getNum() > 3000)
                return;

            if(!ships.removeShip(newNum.getShipModelId(), newNum.getNum()))
                return;

            ShipModel model = PacketService.getShipModel(newNum.getShipModelId());

            if(model == null)
                return;

            if(body < model.getBodyId())
                body = model.getBodyId();

            total += newNum.getNum();

        }

        if(fleet.getCommanderId() != commander.getCommanderId())
            fleet.setCommanderId(commander.getCommanderId());

        fleet.setName(packet.getName().noSpaces());
        fleet.setFleetBody(newTeamBody);
        fleet.setBodyId(body);

        fleet.setTarget(packet.getTarget());
        fleet.setTargetInterval(packet.getTargetInterval());

        if(total <= 0)
            return;

        user.save();
        fleet.save();

        ResponseEditShipTeamPacket response = new ResponseEditShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());

        GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();

        fleetInfo.setShipTeamId(fleet.getShipTeamId());
        fleetInfo.setShipNum(fleet.ships());
        fleetInfo.setBodyId((short) body);
        fleetInfo.setReserve((short) 0);
        fleetInfo.setDirection((char) 0);

        fleetInfo.setPosX((char) fleet.getPosX());
        fleetInfo.setPosY((char) fleet.getPosY());
        fleetInfo.setOwner((char) 2);

        response.setGalaxyFleetInfo(fleetInfo);
        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onArrangement(RequestArrangeShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        List<ShipTeamNum> list = new ArrayList<>();
        UserShips ships = user.getShips();
        ShipTeamNum reference = new ShipTeamNum();

        if(ships.getShips() != null && !ships.getShips().isEmpty())
            for(ShipTeamNum shipTeamNum : ships.getShips())
                list.add(shipTeamNum);

        while(list.size() < 120)
            list.add(reference.trash());

        ResponseArrangeShipTeamPacket response = new ResponseArrangeShipTeamPacket();

        response.setDataLen((short) ships.getShips().size());
        response.setKind((short) 0);

        response.setShipNums(list);
        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onCommanderArrangement(RequestCommanderInfoArrangePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        ResponseCommanderInfoArrangePacket response = new ResponseCommanderInfoArrangePacket();
        List<Commander> commanders = user.getCommanders();

        response.setDataLen(60);

        for(int i = 0; i < commanders.size(); i++)
            if (!commanders.get(i).hasFleet())
                response.getData().set(i, commanders.get(i).getCommanderId());

        System.out.println(response);

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onMoveShipTeam(RequestMoveShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        if(packet.getPosX() < 0 || packet.getPosY() < 0)
            return;

        if(packet.getPosX() > 24 || packet.getPosY() > 24)
            return;

        Fleet fleet = PacketService.getFleetRepository().findByShipTeamId(packet.getShipTeamId());

        if(fleet == null || fleet.getGuid() != packet.getGuid())
            return;

        fleet.setPosX(packet.getPosX());
        fleet.setPosY(packet.getPosY());

        fleet.save();

        ResponseMoveShipTeamPacket response = new ResponseMoveShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(fleet.getGalaxyId());

        response.setShipTeamId(fleet.getShipTeamId());
        response.setPosX(fleet.getPosX());
        response.setPosY(fleet.getPosY());

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onMoveShipTeam(RequestDirectionShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        if(packet.getDirection() < 0 || packet.getDirection() > 3)
            return;

        Fleet fleet = PacketService.getFleetRepository().findByShipTeamId(packet.getShipTeamId());

        if(fleet == null || fleet.getGuid() != packet.getGuid())
            return;

        fleet.setDirection(packet.getDirection());
        fleet.save();

        ResponseDirectionShipTeamPacket response = new ResponseDirectionShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(fleet.getGalaxyId());

        response.setShipTeamId(fleet.getShipTeamId());
        response.setDirection((char) fleet.getDirection());

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onDisbandShipTeam(RequestDisbandShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        Fleet fleet = PacketService.getFleetRepository().findByShipTeamId(packet.getShipTeamId());

        if(fleet == null || fleet.getGuid() != packet.getGuid())
            return;

        UserShips ships = user.getShips();

        for(ShipTeamNum teamNum : fleet.getFleetBody().getCells())
            if(teamNum.getNum() != 0 && teamNum.getShipModelId() > -1)
                ships.addShip(teamNum.getShipModelId(), teamNum.getNum());

        fleet.remove();
        user.save();

        ResponseDeleteShipTeamBroadcastPacket response = new ResponseDeleteShipTeamBroadcastPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(fleet.getGalaxyId());

        response.setShipTeamId(fleet.getShipTeamId());

        packet.getSmartServer().send(response);

    }

}
