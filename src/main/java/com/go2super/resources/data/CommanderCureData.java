package com.go2super.resources.data;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CommanderCureData {

    private int commandType;
    private int commandStar;

    private int hospitalization;

    private double death;
    private double level;

}
