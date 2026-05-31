package com.go2super.resources.data;

import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.resources.data.meta.EnemyFleetMeta;
import com.go2super.resources.data.meta.PlayerFleetMeta;
import com.go2super.resources.data.meta.RewardMeta;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class InstanceData {

    private int id;
    private String name;
    private int experienceGain;

    private List<RewardMeta> rewards;

    private List<PlayerFleetMeta> playerFleets;
    private List<EnemyFleetMeta> fleets;

    public List<BattleFleet> getEnemyFleets() {

        List<BattleFleet> prefab = new ArrayList<>();

        for(EnemyFleetMeta meta : fleets) {

            LayoutData data = meta.getLayoutData();
            BattleFleet fleet = data.getBattleFleet();

            fleet.setDefender(false);
            fleet.setPosX(meta.getX());
            fleet.setPosY(meta.getY());

            prefab.add(fleet);

        }

        return prefab;

    }

}
