package com.go2super.resources.json;

import com.go2super.database.entity.ShipModel;
import com.go2super.resources.data.InstanceData;
import com.go2super.resources.data.LayoutData;
import com.go2super.resources.data.meta.LayoutDesignMeta;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class InstancesJson {

    private List<InstanceData> instance;
    private List<LayoutData> layout;

    public LayoutData getLayout(String nameKey) {
        for(LayoutData data : layout)
            if(data.getName().equals(nameKey))
                return data;
        return null;
    }

    public InstanceData getInstance(int id) {
        for(InstanceData data : instance)
            if(data.getId() == id)
                return data;
        return null;
    }

}
