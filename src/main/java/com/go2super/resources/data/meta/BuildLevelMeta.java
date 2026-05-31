package com.go2super.resources.data.meta;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserBuildings;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class BuildLevelMeta {

    private int lv;
    private int time;

    private int metal;
    private int gas;
    private int gold;

    private List<BuildRequirementMeta> requirements;
    private List<BuildEffectMeta> effects;

    public List<String> getEffectNames() {
        List<String> names = new ArrayList<>();
        for(BuildEffectMeta meta : effects)
            names.add(meta.getType());
        return names;
    }

    public BuildEffectMeta getEffect(String type) {
        for(BuildEffectMeta meta : effects)
            if(meta.getType().equalsIgnoreCase(type))
                return meta;
        return null;
    }

    public boolean canBuild(User user) {

        UserBuildings buildings = user.getBuildings();

        if(user.getResources().getHe3() < gas ||
           user.getResources().getMetal() < metal ||
           user.getResources().getGold() < gold) {
            return false;
        }

        if(buildings != null && requirements != null)
            for(BuildRequirementMeta requirement : requirements)
                if(!buildings.has(requirement.getId(), requirement.getLv()))
                    return false;

        return true;

    }

    public void charge(User user) {

        user.getResources().setHe3(user.getResources().getHe3() - gas);
        user.getResources().setMetal(user.getResources().getMetal() - metal);
        user.getResources().setGold(user.getResources().getGold() - gold);

    }

}
