package com.go2super.service.jobs.user;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserBuilding;
import com.go2super.database.entity.sub.UserBuildings;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.construction.ResponseBuildCompletePacket;
import com.go2super.service.jobs.GalaxyUserJob;

public class BuilderJob implements GalaxyUserJob {

    @Override
    public void run(LoggedGameUser loggedGameUser, User user) {

        UserBuildings buildings = user.getBuildings();

        if(buildings.getBuildings().isEmpty())
            return;

        boolean update = false;

        for(UserBuilding building : buildings.getBuildings())
            if(building.getUpdating() != null && building.getUpdating()) {
                if (building.updatingTime() <= 0) {

                    update = true;
                    building.setLevelId(building.getLevelId() + 1);
                    building.setUpdating(false);
                    building.setUntilUpdate(null);

                    ResponseBuildCompletePacket response = buildCompletePacket(0, user.getPlanet().getPosition().galaxyId(), buildings.getBuildings().indexOf(building));
                    loggedGameUser.getSmartServer().send(response);

                }
            } else if(building.getRepairing() != null && building.getRepairing()) {

                if(building.repairingTime() <= 0) {

                    update = true;
                    building.setRepairing(false);
                    building.setUntilRepair(null);

                    ResponseBuildCompletePacket response = buildCompletePacket(0, user.getPlanet().getPosition().galaxyId(), buildings.getBuildings().indexOf(building));
                    loggedGameUser.getSmartServer().send(response);

                }

            }

        if(update)
            user.save();

    }

    public ResponseBuildCompletePacket buildCompletePacket(int gmap, int gid, int indexId) {

        ResponseBuildCompletePacket buildCompletePacket = new ResponseBuildCompletePacket();

        buildCompletePacket.setGalaxyMapId(gmap);
        buildCompletePacket.setGalaxyId(gid);
        buildCompletePacket.setIndexId(indexId);

        return buildCompletePacket;

    }

}
