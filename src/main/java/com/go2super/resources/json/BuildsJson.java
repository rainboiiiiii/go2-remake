package com.go2super.resources.json;

import com.go2super.resources.data.BuildData;
import com.go2super.resources.data.LotteryData;
import lombok.Data;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Data
@ToString
public class BuildsJson {

    private List<BuildData> buildings;

    public BuildData getBuild(int id) {
        for(BuildData data : buildings)
            if(data.getId() == id)
                return data;
        return null;
    }

}
