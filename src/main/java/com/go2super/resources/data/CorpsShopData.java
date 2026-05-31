package com.go2super.resources.data;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CorpsShopData {

    private int shipModelId;
    private String name;

    private int minLevel;
    private int cost;
    private int amount;

}
