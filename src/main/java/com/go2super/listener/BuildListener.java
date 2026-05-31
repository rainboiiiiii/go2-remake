package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserBuilding;
import com.go2super.database.entity.sub.UserBuildings;
import com.go2super.logger.BotLogger;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.construction.*;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.BuildData;
import com.go2super.resources.data.meta.BuildLevelMeta;
import com.go2super.resources.json.BuildsJson;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;

import java.sql.Date;
import java.time.Instant;

public class BuildListener implements PacketListener {

    @PacketProcessor
    public void onBuildMove(RequestMoveBuildPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null || packet.getIndexId() < 0)
            return;

        UserBuilding building = user.getBuildings().getBuilding(packet.getIndexId());

        building.setX(packet.getPosX().getValue());
        building.setY(packet.getPosY().getValue());

        user.save();

        ResponseMoveBuildPacket response = new ResponseMoveBuildPacket();

        response.setIndexId(packet.getIndexId());
        response.setPosX(packet.getPosX());
        response.setPosY(packet.getPosY());

        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onBuild(RequestCreateBuildPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null)
            return;

        BuildsJson json = ResourceManager.getBuilds();
        BuildData data = json.getBuild(packet.getBuildingId());

        if(data == null)
            return;

        BuildLevelMeta level;
        UserBuildings buildings = user.getBuildings();
        UserBuilding building = buildings.getBuilding(packet.getIndexId());

        int index = -1;

        if(building == null) {

            int count = buildings.count(packet.getBuildingId());

            if(count + 1 > data.getLimit())
                return;

            level = data.getLevel(0);

            if(level == null || !level.canBuild(user))
                return;

            level.charge(user);

            building = UserBuilding.builder()
                    .buildingId(packet.getBuildingId())
                    .levelId(-1)
                    .repairing(false)
                    .updating(true)
                    .untilUpdate(DateUtil.now(level.getTime()))
                    .x(packet.getPosX().getValue())
                    .y(packet.getPosY().getValue())
                    .build();

            user.getBuildings().getBuildings().add(building);
            index = user.getBuildings().getBuildings().size() - 1;

        } else {

            level = data.getLevel(building.getLevelId() + 1);

            if(level == null || !level.canBuild(user))
                return;

            level.charge(user);

            building.setUpdating(true);
            building.setUntilUpdate(DateUtil.now(level.getTime()));

            index = user.getBuildings().getBuildings().indexOf(building);

        }

        if(level == null)
            return;

        ResponseCreateBuildPacket response = ResponseCreateBuildPacket.builder()
                .buildingId(building.getBuildingId())
                .indexId(index)
                .levelId(building.getLevelId())
                .he3(level.getGas())
                .metal(level.getMetal())
                .money(level.getGold())
                .needTime(level.getTime())
                .build();

        user.save();
        packet.getSmartServer().send(response);

    }

    @PacketProcessor
    public void onSpeedUp(RequestSpeedBuildPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = PacketService.getUserRepository().findByGuid(packet.getGuid());

        if(user == null || packet.getIndexId() < 0)
            return;

        UserBuildings buildings = user.getBuildings();

        if(buildings.getBuildings().size() <= packet.getIndexId())
            return;

        UserBuilding building = buildings.getBuildings().get(packet.getIndexId());

        if(building == null || !building.getUpdating())
            return;

        int price = 0;
        int reduce = 0;

        int spare = building.updatingTime().intValue();

        boolean complete = false;
        boolean voucher = packet.getKind() == 1;

        switch(packet.getBuildingSpeedId()) {

            case 0:

                reduce = 30 * 60;
                price = 3;

                break;

            case 1:

                reduce = 2 * 60 * 60;
                price = 12;

                break;

            case 2:

                reduce = 8 * 60 * 60;
                price = 48;

                break;

            case 3:

                reduce = 24 * 60 * 60;
                price = 144;

                break;

            default:

                reduce = spare;
                price = (spare / 60 / 10) + 1;

                if(price <= 0)
                    price = 1;

                break;

        }

        if(spare - reduce <= 0)
            complete = true;

        boolean paid = false;

        if(voucher) {
            if (user.getResources().getVouchers() >= price) {

                user.getResources().setVouchers(user.getResources().getVouchers() - price);
                paid = true;

            }
        } else {
            if (user.getResources().getMallPoints() >= price) {

                user.getResources().setMallPoints(user.getResources().getMallPoints() - price);
                paid = true;

            }
        }

        if(paid) {

            long time = building.getUntilUpdate().getTime();
            time -= (reduce * 1000) + 1;

            building.setUntilUpdate(Date.from(Instant.ofEpochMilli(time)));
            user.save();

            ResponseSpeedBuildPacket response = new ResponseSpeedBuildPacket();

            response.setBuildingSpeedId(packet.getBuildingSpeedId());
            response.setCredit(price);
            response.setIndexId(packet.getIndexId());
            response.setTime(complete ? 0 : reduce);

            BotLogger.dev("Reduction = " + reduce + ", Price = " + price);

            packet.getSmartServer().send(response);

        }



    }

}
