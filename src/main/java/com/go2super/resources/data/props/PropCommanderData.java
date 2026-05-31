package com.go2super.resources.data.props;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PropCommanderData extends PropMetaData {

    private int skillId;
    private int type;

}
