package com.go2super.service.battle;

import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BattleFleetTeam {

    private List<BattleFleetCell> cells = new ArrayList<>();

    public static BattleFleetTeam fromShipTeamBody(int guid, ShipTeamBody body) {

        BattleFleetTeam team = new BattleFleetTeam();

        for(ShipTeamNum num : body.getCells())
            team.getCells().add(BattleFleetCell.getByNum(guid, num));

        return team;

    }

}
