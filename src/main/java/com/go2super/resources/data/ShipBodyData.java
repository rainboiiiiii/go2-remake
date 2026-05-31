package com.go2super.resources.data;

import com.go2super.resources.data.meta.BodyLevelMeta;
import com.go2super.resources.data.meta.PartLevelMeta;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ShipBodyData {

    private int groupId;

    private String name;
    private String bodyType;
    private String armorType;

    private List<BodyLevelMeta> levels;

    public BodyLevelMeta getLevel(int id) {
        for(BodyLevelMeta meta : levels)
            if(meta.getId() == id)
                return meta;
        return null;
    }

}
