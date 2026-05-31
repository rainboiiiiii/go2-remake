package com.go2super.resources.data;

import com.go2super.resources.data.props.*;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class PropData {

    private int id;
    private String name;
    private String type;

    private PropMetaData data;
    private PropMallData[] mall;

    private int salvage = -1;

    public PropGemData getGemData() {
        return (PropGemData) data;
    }

    public PropCommanderData getCommanderData() {
        return (PropCommanderData) data;
    }

    public PropScrollData getScrollData() {
        return (PropScrollData) data;
    }

    public PropBuffData getBuffData() {
        return (PropBuffData) data;
    }

    public PropMetaData getData() {
        return data;
    }

    public PropMallData[] getMallData() {
        return mall;
    }

    public boolean hasCommanderData() {
        return data != null;
    }

    public boolean hasMallData() {
        return mall != null && mall.length > 0;
    }

    public boolean hasCorsairValue() {
        return salvage != -1;
    }

}
