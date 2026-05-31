package com.go2super.resources.data;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.resources.data.meta.LayoutCommanderMeta;
import com.go2super.resources.data.meta.LayoutDesignMeta;
import com.go2super.service.BattleService;
import com.go2super.service.battle.BattleFleetTeam;
import lombok.Data;
import lombok.ToString;

import java.util.LinkedList;

@Data
@ToString
public class LayoutData {

    private String name;

    private LinkedList<LayoutDesignMeta> layout;
    private LayoutCommanderMeta commander;

    private String targetPriority;
    private String targetRange;

    public BattleFleet getBattleFleet() {

        BattleFleet battleFleet = new BattleFleet();
        ShipTeamBody teamBody = getTeamBody();

        battleFleet.setBodyId(teamBody.getRenderedBody());
        battleFleet.setDirection(0);
        battleFleet.setHe3(300);
        battleFleet.setMaxHe3(300);
        battleFleet.setMaxRounds(10);
        battleFleet.setJoinRound(0);
        battleFleet.setShipTeamId(-1);
        battleFleet.setTeam(BattleFleetTeam.fromShipTeamBody(-1, teamBody));
        battleFleet.setTarget(BattleService.getTarget(targetRange));
        battleFleet.setTargetInterval(BattleService.getTargetInterval(targetPriority));
        battleFleet.setBattleCommander(commander.getBattleCommander());

        return battleFleet;

    }

    public ShipTeamBody getTeamBody(){

        ShipTeamBody body = new ShipTeamBody();

        for(LayoutDesignMeta design : layout) {

            if(design == null) {
                body.getCells().add(new ShipTeamNum(-1, 0));
                continue;
            }

            ShipModel model = design.getModel();
            body.getCells().add(new ShipTeamNum(model.getShipModelId(), design.getAmount()));

        }

        return body;

    }

}
