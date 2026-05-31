package com.go2super.resources.data;

import com.go2super.resources.data.meta.PartLevelMeta;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ShipPartData {

    private int groupId;
    private String name;

    private String partType;
    private String partSubType;

    private int limit;
    private List<PartLevelMeta> levels;

    public PartLevelMeta getLevel(int id) {
        for(PartLevelMeta meta : levels)
            if(meta.getId() == id)
                return meta;
        return null;
    }

}
