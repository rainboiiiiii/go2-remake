package com.go2super.database.entity.sub;

import com.go2super.obj.game.CreateShipInfo;
import com.go2super.obj.game.ShipTeamNum;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class UserShips {

    public List<ShipTeamNum> ships = new ArrayList<>();
    public List<FactoryShip> factory = new ArrayList<>();

    public List<ShipTeamNum> getShips() {
        if(ships == null)
            ships = new ArrayList<>();
        return ships;
    }

    public List<FactoryShip> getFactory() {
        if(factory == null)
            factory = new ArrayList<>();
        return factory;
    }

    public List<CreateShipInfo> getFactoryAsBuffer() {

        List<CreateShipInfo> infos = new ArrayList<>();

        for(FactoryShip factoryShip : getFactory())
            infos.add(factoryShip.packet());

        return infos;

    }

    public boolean fabricate(int shipModel, int num, int buildTime) {

        if(getFactory().size() >= 5)
            return false;

        getFactory().add(FactoryShip.of(shipModel, num, buildTime));
        return true;

    }

    public void addShip(int shipModel, int num) {
        for(ShipTeamNum shipTeamNum : getShips())
            if(shipTeamNum.getShipModelId() == shipModel) {
                shipTeamNum.setNum(shipTeamNum.getNum() + num);
                return;
            }
        getShips().add(new ShipTeamNum(shipModel, num));
    }

    public boolean removeShip(int shipModel, int num) {

        for(ShipTeamNum shipTeamNum : getShips())
            if(shipTeamNum.getShipModelId() == shipModel) {

                shipTeamNum.setNum(shipTeamNum.getNum() - num);

                if(shipTeamNum.getNum() < 0)
                    return false;
                else if(shipTeamNum.getNum() == 0) {
                    getShips().remove(shipTeamNum);
                    return true;
                }

                return true;

            }

        return false;

    }

    public int countStoredShips() {

        int result = 0;

        for(ShipTeamNum team : getShips())
            result += team.getNum();

        return result;

    }

}
