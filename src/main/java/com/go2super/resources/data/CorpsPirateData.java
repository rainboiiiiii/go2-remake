package com.go2super.resources.data;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CorpsPirateData {

    private String name;
    private String comment;

    private int level;
    private int corpsLevel;
    private int honor;
    private int wealth;
    private int shipTeamNum;

}
