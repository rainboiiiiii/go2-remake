package com.go2super.service.jobs.user;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.FactoryShip;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.ship.ResponseShipCreatingCompletePacket;
import com.go2super.service.jobs.GalaxyUserJob;
import com.go2super.socket.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class ShipConstructionJob implements GalaxyUserJob {
    
    @Override
    public void run(LoggedGameUser loggedGameUser, User user) {

        UserShips ships = user.getShips();

        if(ships == null || ships.getFactory() == null || ships.getFactory().isEmpty())
            return;

        List<FactoryShip> factoryShips = ships.getFactory();
        List<FactoryShip> toDelete = new ArrayList<>();

        boolean save = false;

        for(FactoryShip factoryShip : factoryShips) {

            if(factoryShip.getUntil() == null) {

                save = true;
                toDelete.add(factoryShip);
                continue;

            }

            while(factoryShip.getUntil() != null && DateUtil.remains(factoryShip.getUntil()).intValue() <= 0) {

                factoryShip.setUntil(DateUtil.now(factoryShip.getBuildTime()));
                factoryShip.setNum(factoryShip.getNum() - 1);

                if(factoryShip.getNum() == 0) {

                    factoryShip.setUntil(null);
                    toDelete.add(factoryShip);

                }

                ships.addShip(factoryShip.getShipModelId(), 1);

                ResponseShipCreatingCompletePacket packet = new ResponseShipCreatingCompletePacket();
                packet.setIndexId(factoryShips.indexOf(factoryShip));

                loggedGameUser.getSmartServer().send(packet);
                save = true;

            }

        }

        factoryShips.removeAll(toDelete);

        if(save)
            user.save();

    }

}
