package com.go2super.service.battle;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartLevelMeta;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Data
public class BattleFleetWeapon {

    private int guid = -1;

    private int weaponId;
    private int amount;

    private int reload;
    private int lastShoot;

    private int minAttack;
    private int maxAttack;

    private int maxRange;
    private int minRange;

    public ShipPartData getData() {
        return ResourceManager.getShipParts().findByPartId(weaponId);
    }

    public static BattleFleetWeapon getByPart(int guid, int id, ShipPartData data) {

        BattleFleetWeapon weapon = new BattleFleetWeapon();
        PartLevelMeta level = data.getLevel(id);

        weapon.setWeaponId(id);
        weapon.setAmount(1);
        weapon.setGuid(guid);
        weapon.setLastShoot(-1);

        weapon.setReload((int) level.getEffectValue("reload"));

        weapon.setMinRange((int) level.getEffectMin("range"));
        weapon.setMaxRange((int) level.getEffectMax("range"));


        weapon.setMinAttack((int) level.getEffectMin("attack"));
        weapon.setMaxAttack((int) level.getEffectMax("attack"));

        return weapon;

    }

    public static List<BattleFleetWeapon> getByNum(int guid, BattleFleetCell cell) {

        List<BattleFleetWeapon> weapons = new ArrayList<>();

        for(Pair<Integer, ShipPartData> data : cell.getAttackParts()) {

            Optional<BattleFleetWeapon> weapon = weapons.stream().filter(w -> data.getKey() == w.getWeaponId()).findAny();

            if(!weapon.isPresent()) {

                weapons.add(getByPart(guid, data.getKey(), data.getValue()));
                continue;

            }

            weapon.get().setAmount(weapon.get().getAmount() + 1);

        }

        return weapons;

    }

}
