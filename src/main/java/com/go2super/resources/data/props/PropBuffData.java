package com.go2super.resources.data.props;

import com.go2super.resources.data.meta.BuffMeta;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class PropBuffData extends PropMetaData {

    private List<BuffMeta> buffs;

}
