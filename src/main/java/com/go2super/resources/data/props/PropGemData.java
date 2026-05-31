package com.go2super.resources.data.props;

import com.go2super.resources.data.meta.GemMeta;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class PropGemData extends PropMetaData {

    private int type;
    private int level;
    private int color;

    private List<GemMeta> stats;

}
