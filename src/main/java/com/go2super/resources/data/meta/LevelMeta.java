package com.go2super.resources.data.meta;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LevelMeta {

    private String imageName;
    private int levelId;
    private String occupationSpace;
    private String levelComment;
    private int time;
    private int costMetal;
    private int costHelium3;
    private int costFunds;
    private int metalResourcesNum;
    private int he3resourcesNum;

}
