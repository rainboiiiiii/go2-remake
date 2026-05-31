package com.go2super.resources.data;

import com.go2super.resources.data.meta.BuildLevelMeta;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class BuildData {

    private int id;
    private String name;

    private int limit;
    private List<BuildLevelMeta> levels;

    public BuildLevelMeta getLevel(int level) {
        for(BuildLevelMeta meta : levels)
            if(meta.getLv() == level)
                return meta;
        return null;
    }

}
