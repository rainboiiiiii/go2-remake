package com.go2super.resources.data;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CommanderPullulateData {

    private int commandType;
    private int commandStar;

    private int minPullulate;
    private int maxPullulate;

    private int frigate;
    private int cruiser;
    private int warship;

}
