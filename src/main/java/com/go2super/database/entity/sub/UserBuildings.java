package com.go2super.database.entity.sub;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.BuildData;
import com.go2super.resources.data.meta.BuildLevelMeta;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Data
@Builder
@ToString
public class UserBuildings {

    private LinkedList<UserBuilding> buildings;

    public int count(int building) {
        int result = 0;
        for(UserBuilding cache : buildings)
            if(cache.getBuildingId() == building)
                result++;
        return result;
    }

    public int getHe3Gain() {
        return (int) getBuildingEffectSum("gainFuel");
    }

    public int getMetalGain() {
        return (int) getBuildingEffectSum("gainMetal");
    }

    public int getGoldGain() {
        return (int) getBuildingEffectSum("gainGold");
    }

    public double getGoldBonus() {
        return getBuildingEffectSum("goldBonus");
    }

    public double getMetalBonus() {
        return getBuildingEffectSum("metalBonus");
    }

    public double getHe3Bonus() {
        return getBuildingEffectSum("fuelBonus");
    }

    private double getBuildingEffectSum(String effect) {

        double gain = 0;
        List<UserBuilding> buildings = getBuildingsByEffects(effect);

        for(UserBuilding building : buildings) {

            BuildLevelMeta meta = building.getLevelData();
            gain += meta.getEffect(effect).getValue();

        }

        return gain;

    }

    public List<UserBuilding> getBuildingsByEffects(String...effects) {

        List<UserBuilding> buildings = new ArrayList<>();

        delta : for(UserBuilding building : getBuildings()) {

            BuildData data = building.getData();

            if (building.getLevelId() >= 0) {

                BuildLevelMeta level = data.getLevel(building.getLevelId());

                for (String effect : effects)
                    if (level.getEffectNames().contains(effect)) {

                        buildings.add(building);
                        continue delta;

                    }

            }
        }

        return buildings;

    }

    public UserBuilding getBuilding(int index) {
        if(buildings.size() > index && index > -1)
            return buildings.get(index);
        return null;
    }

    public List<UserBuilding> getBuildings(int id) {
        List<UserBuilding> filter = new ArrayList<>();
        for(UserBuilding building : buildings)
            if(building.getBuildingId() == id)
                filter.add(building);
        return filter;
    }

    public boolean has(int id, int level) {
        for(UserBuilding building : getBuildings(id))
            if(building.getLevelId() == level)
                return true;
        return false;
    }

    public UserBuildings addBuilding(int building, int level, int x, int y) {
        return addBuilding(UserBuilding.builder().buildingId(building).levelId(level).x(x).y(y).build());
    }

    public UserBuildings addBuilding(UserBuilding building) {

        if(buildings == null)
            buildings = new LinkedList<>();

        buildings.add(building);
        return this;

    }

}
