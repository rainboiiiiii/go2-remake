package com.go2super.resources.json;

import com.go2super.resources.data.LevelData;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class LevelsJson {

    private List<LevelData> levels;

    public int getLevel(int exp) {
        int level = 0;
        for(LevelData data : levels)
            if(data.getExp() < exp)
                level++;
        return level;
    }

}
