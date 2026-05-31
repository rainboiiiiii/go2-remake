package com.go2super.resources.data.meta;

import lombok.Data;

@Data
public class BodyEffectMeta {

    private String type;
    private Object value;

    private double max;
    private double min;

}
