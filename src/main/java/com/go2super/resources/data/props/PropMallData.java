package com.go2super.resources.data.props;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PropMallData extends PropMetaData {

    private int amount;
    private boolean bound;
    private int currency;
    private int value;

}
