package com.go2super.resources.data.meta;

import lombok.Data;

import java.util.List;

@Data
public class BodyLevelMeta {

    private int id;
    private int lv;

    private BuildCostMeta buildCost;

    private List<PartEffectMeta> effects;
    private UpgradeMeta upgrade;

    public PartEffectMeta getEffect(String attribute) {
        for(PartEffectMeta meta : effects)
            if(meta.getType().equalsIgnoreCase(attribute))
                return meta;
        return null;
    }

    public boolean hasEffect(String attribute) {
        return getEffect(attribute) != null;
    }

}
