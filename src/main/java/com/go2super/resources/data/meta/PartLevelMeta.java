package com.go2super.resources.data.meta;

import lombok.Data;

import java.util.List;

@Data
public class PartLevelMeta {

    private int id;
    private int lv;

    private BuildCostMeta buildCost;

    private double fuelUsage;
    private int moduleUsage;

    private List<PartEffectMeta> effects;
    private UpgradeMeta upgrade;

    public double getEffectMax(String attribute) {
        PartEffectMeta meta = getEffect(attribute);
        return meta != null ? meta.getMax() : 0;
    }

    public double getEffectMin(String attribute) {
        PartEffectMeta meta = getEffect(attribute);
        return meta != null ? meta.getMin() : 0;
    }

    public double getEffectValue(String attribute) {
        PartEffectMeta meta = getEffect(attribute);
        return meta != null ? (double) meta.getValue() : 0;
    }

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
