package com.go2super.service.battle;

import com.go2super.database.entity.sub.BattleFleet;

import com.go2super.service.battle.type.TargetInterval;
import lombok.Data;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@ToString
public class BattleCell {

    private LinkedList<BattleFleet> attackers = new LinkedList<>();
    private LinkedList<BattleFleet> defenders = new LinkedList<>();

    private int attackerStars = -1;
    private int maxAttackerAttack = -1;
    private int minAttackerAttack = -1;
    private int maxAttackerDurability = -1;
    private int minAttackerDurability = -1;

    private int defenderStars = -1;
    private int maxDefenderAttack = -1;
    private int minDefenderAttack = -1;
    private int maxDefenderDurability = -1;
    private int minDefenderDurability = -1;

    private int x = -1;
    private int y = -1;

    public void addFleet(BattleFleet fleet) {

        fleet.reset();
        fleet.calculate();

        int attack = fleet.calculateAttack();
        int durability = fleet.getRoundDurability();
        int commander = fleet.getBattleCommander().getStars();

        if(x == -1)
            x = fleet.getPosX();

        if(y == -1)
            y = fleet.getPosY();

        if(fleet.isAttacker()) {

            if(attackerStars < commander)
                attackerStars = commander;

            if(maxAttackerAttack < attack || minAttackerAttack == -1)
                maxAttackerAttack = attack;

            if(minAttackerAttack > attack || minAttackerAttack == -1)
                minAttackerAttack = attack;

            if(maxAttackerDurability < durability || maxAttackerDurability == -1)
                maxAttackerDurability = durability;

            if(minAttackerDurability > durability || minAttackerDurability == -1)
                minAttackerDurability = durability;

            attackers.add(fleet);
            return;

        }

        if(defenderStars < commander)
            defenderStars = commander;

        if(maxDefenderAttack < attack || minDefenderAttack == -1)
            maxDefenderAttack = attack;

        if(minDefenderAttack > attack || minDefenderAttack == -1)
            minDefenderAttack = attack;

        if(maxDefenderDurability < durability || maxDefenderDurability == -1)
            maxDefenderDurability = durability;

        if(minDefenderDurability > durability || minDefenderDurability == -1)
            minDefenderDurability = durability;

        defenders.add(fleet);
        return;

    }

    public String getCharacter() {

        if(isEmpty())
            return "..";

        return attackers.size() > 0 && defenders.size() > 0 ? "ZZ" : (attackers.size() > 0) ? "AF" : "DF";

    }

    public int getEnemyAttribute(boolean attacker, TargetInterval interval) {

        switch(interval) {

            case COMMANDER:
                return attacker ? defenderStars : attackerStars;

            case MAX_ATTACK_POWER:
                return attacker ? maxDefenderAttack : maxAttackerAttack;

            case MIN_ATTACK_POWER:
                return attacker ? minDefenderAttack : minAttackerAttack;

            case MAX_DURABILITY:
                return attacker ? maxDefenderDurability : maxAttackerDurability;

            case MIN_DURABILITY:
                return attacker ? minDefenderDurability : minAttackerDurability;

        }

        return -1;

    }

    public BattleFleet getEnemyCandidate(BattleFleet attacker) {

        switch(attacker.getFleetTargetInterval()) {

            case CLOSEST:
                return attacker.isAttacker() ? defenders.get(0) : attackers.get(0);

            case COMMANDER:
                return getEnemyByCommander(attacker.isAttacker());

            case MAX_ATTACK_POWER:
                return getEnemyByMaxAttack(attacker.isAttacker());

            case MIN_ATTACK_POWER:
                return getEnemyByMinAttack(attacker.isAttacker());

            case MAX_DURABILITY:
                return getEnemyByMaxDurability(attacker.isAttacker());

            case MIN_DURABILITY:
                return getEnemyByMinDurability(attacker.isAttacker());

        }

        return getEnemies(attacker.isAttacker()).get(0);

    }

    public BattleFleet getEnemyByCommander(boolean attacker) {

        List<BattleFleet> fleets = getEnemies(attacker);
        int value = attacker ? defenderStars : attackerStars;

        for(BattleFleet fleet : fleets)
            if(fleet.getBattleCommander().getStars() == value)
                return fleet;

        return fleets.get(0);

    }

    public BattleFleet getEnemyByMaxAttack(boolean attacker) {

        List<BattleFleet> fleets = getEnemies(attacker);
        int value = attacker ? maxDefenderAttack : maxAttackerAttack;

        for(BattleFleet fleet : fleets)
            if(fleet.calculateAttack() == value)
                return fleet;

        return fleets.get(0);

    }

    public BattleFleet getEnemyByMinAttack(boolean attacker) {

        List<BattleFleet> fleets = getEnemies(attacker);
        int value = attacker ? minDefenderAttack : minAttackerAttack;

        for(BattleFleet fleet : fleets)
            if(fleet.calculateAttack() == value)
                return fleet;

        return fleets.get(0);

    }

    public BattleFleet getEnemyByMaxDurability(boolean attacker) {

        List<BattleFleet> fleets = getEnemies(attacker);
        int value = attacker ? maxDefenderDurability : maxAttackerDurability;

        for(BattleFleet fleet : fleets)
            if(fleet.getRoundDurability() == value)
                return fleet;

        return fleets.get(0);

    }

    public BattleFleet getEnemyByMinDurability(boolean attacker) {

        List<BattleFleet> fleets = getEnemies(attacker);
        int value = attacker ? minDefenderDurability : minAttackerDurability;

        for(BattleFleet fleet : fleets)
            if(fleet.getRoundDurability() == value)
                return fleet;

        return fleets.get(0);

    }

    public List<BattleFleet> getEnemies(boolean attacker) {
        return attacker ? getDefenders() : getAttackers();
    }

    public boolean isEmpty() {
        return attackers.isEmpty() && defenders.isEmpty();
    }

    public boolean hasEnemies(BattleFleet compare) {

        if(compare.isAttacker())
            return defenders.size() > 0;

        return attackers.size() > 0;

    }

    public boolean hasAllies(BattleFleet compare) {

        if(compare.isAttacker())
            return attackers.size() > 0;

        return defenders.size() > 0;

    }

    public int getDistance(BattleFleet fleet) {
        return Math.abs(fleet.getPosX() - getX()) + Math.abs(fleet.getPosY() - getY());
    }

    public int getDistance(BattleCell cell) {
        return Math.abs(cell.getX() - getX()) + Math.abs(cell.getY() - getY());
    }

    public List<BattleFleet> getAllFleets() {
        return Stream.concat(attackers.stream(), defenders.stream()).collect(Collectors.toList());
    }

}
