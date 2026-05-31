package com.go2super.database.entity.sub;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.ShipModel;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.service.PacketService;
import com.go2super.service.battle.BattleCell;
import com.go2super.service.battle.BattleFleetCell;
import com.go2super.service.battle.BattleFleetTeam;
import com.go2super.service.battle.astar.Node;
import com.go2super.service.battle.type.Target;
import com.go2super.service.battle.type.TargetInterval;
import lombok.Data;

import java.util.Arrays;

@Data
public class BattleFleet implements Comparable<BattleFleet> {

    private BattleCommander battleCommander;
    private int guid;
    private int shipTeamId;

    private int he3;
    private int maxHe3;

    private int minRange;
    private int maxRange;

    private int bodyId;
    private int target;
    private int targetInterval;

    private int posX;
    private int posY;

    private int direction;
    private int maxRounds;
    private int joinRound;

    private int roundAttack;
    private int roundDurability;

    private int movement;

    private BattleFleetTeam team;
    private boolean defender;
    private boolean destroyed;

    private Fleet getRealFleet() {
        return PacketService.getFleetRepository().findByShipTeamId(shipTeamId);
    }

    public void calculate() {
        calculateDurability();
        calculateAttack();
        calculateRanges();
        calculateMovement();
    }

    public void reset() {
        roundAttack = 0;
        roundDurability = 0;
    }

    public int calculateDurability() {

        if(roundDurability != 0)
            return roundDurability;

        for(BattleFleetCell cell : team.getCells())
            if(cell.getShipModelId() >= -1 && cell.getAmount() > 0)
                roundDurability += PacketService.getShipModel(cell.getShipModelId()).getDurability() * cell.getAmount();

        return roundDurability;

    }

    public void calculateRanges() {

        for(BattleFleetCell cell : team.getCells())
            if(cell.getShipModelId() >= -1 && cell.getAmount() > 0) {

                ShipModel model = PacketService.getShipModel(cell.getShipModelId());

                if(minRange == 0)
                    minRange = model.getMinRange();
                else if(minRange > model.getMinRange())
                    minRange = model.getMinRange();

                if(maxRange == 0)
                    maxRange = model.getMaxRange();
                else if(maxRange < model.getMaxRange())
                    maxRange = model.getMaxRange();

            }

    }

    public int calculateAttack() {

        if(roundAttack != 0)
            return roundAttack;

        for(BattleFleetCell cell : team.getCells())
            if(cell.getShipModelId() >= -1 && cell.getAmount() > 0)
                roundAttack += PacketService.getShipModel(cell.getShipModelId()).getMinAttack() * cell.getAmount();

        return roundAttack;

    }

    public int calculateMovement() {

        int result = Integer.MAX_VALUE;

        for(BattleFleetCell cell : team.getCells())
            if(!cell.isEmpty()) {

                int cache = cell.getMovement();

                if (cache < result)
                    result = cache;

            }

        if(result == Integer.MAX_VALUE)
            result = 0;

        movement = result;
        return movement;


    }

    public boolean isDefender() {
        return defender;
    }

    public boolean isAttacker() {
        return !defender;
    }

    public int getTeam() {
        return isAttacker() ? 1 : 0;
    }

    private int ships() {
        int number = 0;
        for(BattleFleetCell cell : team.getCells())
            if(cell.getShipModelId() >= -1 && cell.getAmount() > 0)
                number += cell.getAmount();
        return number;
    }

    public int getMinRange() {
        return minRange;
    }

    public int getMaxRange() {
        return maxRange;
    }

    public int getCurrentRange() {
        if(getFleetTarget() == Target.MAX_RANGE)
            return getMaxRange();
        return getMinRange();
    }

    public Target getFleetTarget() {
        return Arrays.asList(Target.values()).get(target);
    }

    public TargetInterval getFleetTargetInterval() {
        return Arrays.asList(TargetInterval.values()).get(target);
    }

    public BattleCell getCell(BattleCell[][] cells) {
        return cells[getPosX()][getPosY()];
    }

    public Node getNode() {
        return new Node(getPosX(), getPosY());
    }

    @Override
    public int compareTo(BattleFleet fleet) {
        if(getBattleCommander().getTotalSpeed() > fleet.getBattleCommander().getTotalSpeed())
            return -1;
        else if(getBattleCommander().getTotalSpeed() < fleet.getBattleCommander().getTotalSpeed())
            return 1;
        else
            return 0;
    }

}
