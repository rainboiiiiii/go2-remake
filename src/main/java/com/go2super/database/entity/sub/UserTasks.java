package com.go2super.database.entity.sub;

import com.go2super.obj.game.CreateShipInfo;
import com.go2super.obj.game.ShipTeamNum;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class UserTasks {

    public List<ShipTeamNum> ships = new ArrayList<>();
    public List<FactoryShip> factory = new ArrayList<>();

    public List<CreateShipInfo> getFactoryAsBuffer() {

        List<CreateShipInfo> infos = new ArrayList<>();

        for(FactoryShip factoryShip : factory)
            infos.add(factoryShip.packet());

        return infos;

    }

    public int getCurrentShips() {

        int result = 0;

        for(ShipTeamNum team : ships)
            result += team.getNum();

        return result;

    }

}
