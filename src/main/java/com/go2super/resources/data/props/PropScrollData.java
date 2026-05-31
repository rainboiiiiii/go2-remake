package com.go2super.resources.data.props;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PropScrollData extends PropMetaData {

    private int skillId;
    private int[] srcSkillIds;

}
